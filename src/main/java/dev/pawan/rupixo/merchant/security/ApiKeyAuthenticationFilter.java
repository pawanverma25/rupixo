package dev.pawan.rupixo.merchant.security;

import dev.pawan.rupixo.merchant.cache.ApiKeyCache;
import dev.pawan.rupixo.merchant.cache.ApiKeyCacheEntry;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import dev.pawan.rupixo.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String BASIC = "Basic ";
    private final MerchantContext merchantContext;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming Request : {}", request.getRequestURI());

        try{
            final String apiKeyAuthToken = request.getHeader("Authorization");

            if(apiKeyAuthToken == null || !apiKeyAuthToken.startsWith(BASIC)) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decodeApiKeyAndSecret(apiKeyAuthToken);
            if(credentials == null || credentials.length != 2) {
                throw new BadRequestException("Invalid API key format.");
            }

            String keyId = credentials[0];
            String secret = credentials[1];

            ApiKeyCacheEntry apiKeyCacheEntry = apiKeyCache.get(keyId).orElseGet(() -> loadAndCacheApiKey(keyId));

//            ApiKey apiKey = apiKeyRepository.findById(UUID.fromString(keyId))
//                    .orElseThrow(() -> new BadRequestException("Invalid API key."));

            if(apiKeyCacheEntry == null || !apiKeyCacheEntry.enabled() || !secretMatches(apiKeyCacheEntry, secret)) {
                throw new BadRequestException("API key is invalid or missing.");
            }

            var auth = new UsernamePasswordAuthenticationToken(keyId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_KEY_USER")));

            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKeyCacheEntry.merchantId());
            merchantContext.setKeyId(keyId);

            filterChain.doFilter(request, response);
        } catch (Exception e){
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private ApiKeyCacheEntry loadAndCacheApiKey(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);

        if(apiKey == null) return null;
        ApiKeyCacheEntry cacheEntry = new ApiKeyCacheEntry(
                apiKey.getId(),
                apiKey.getMerchant().getId(),
                apiKey.getKeyId(),
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getEnvironment(),
                apiKey.isEnabled(),
                apiKey.getGracePeriodExpiresAt()
        );

        apiKeyCache.put(keyId, cacheEntry);
        return cacheEntry;
    }

    private boolean secretMatches(ApiKeyCacheEntry apiKeyCacheEntry, String rawSecret) {
        if(passwordEncoder.matches(rawSecret, apiKeyCacheEntry.keySecretHash())){
            return false;
        }

        return apiKeyCacheEntry.isInGracePeriod()
                && apiKeyCacheEntry.previousKeySecretHash() != null
                && passwordEncoder.matches(rawSecret, apiKeyCacheEntry.previousKeySecretHash());
    }

    private String[] decodeApiKeyAndSecret(String apiKey) {
        // Remove the "Basic " prefix
        String base64Credentials = apiKey.substring(BASIC.length());
        // Decode the Base64 encoded string
        String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        int colonIndex = decoded.indexOf(":");
        if (colonIndex < 1) return null;

        return new String[]{decoded.substring(0, colonIndex), decoded.substring(colonIndex + 1)};
    }
}
