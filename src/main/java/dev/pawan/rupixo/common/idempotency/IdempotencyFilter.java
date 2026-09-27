package dev.pawan.rupixo.common.idempotency;

import dev.pawan.rupixo.common.exception.IdempotencyConflictException;
import dev.pawan.rupixo.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final List<String> GUARDED_METHODS = List.of("POST", "PUT", "PATCH");
    public static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    public static final Duration COMPLETED_TTL = Duration.ofHours(24);
    public static final String SEPARATOR = "||";

    private final IdempotencyStore store;
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if(!GUARDED_METHODS.contains(request.getMethod())){
            chain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-Idempotency-Key");
        if(rawKey == null || rawKey.isBlank()){
            chain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String key = merchantId == null ? rawKey : merchantId + ":" + rawKey;

        boolean claimed = store.setIfAbsent(key, IN_PROGRESS_TTL);
        if(!claimed){
            // another thread has already claimed this key, check if it has a result
            Optional<String> existingResult = store.get(key);
            if(existingResult.isPresent() && !IdempotencyStore.IN_PROGRESS.equals(existingResult.get())){
               replay(request, response, existingResult.get());
            } else {
                // it is in progress, so we throw an exception to indicate that the request is already being processed
                var ex = new IdempotencyConflictException("Request is in progress");
                //TODO: Handle exception properly in global exception handler
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            byte[] bodyBytes = wrappedResponse.getContentAsByteArray();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            if(status < 400 && bodyBytes.length > 0){
                String storedValue = status + SEPARATOR + body;
                store.store(key, storedValue, COMPLETED_TTL);
                log.debug("Idempotency Filter: Stored idempotency result for key {}: {}", key, storedValue);
            } else {
                store.delete(key);
                log.debug("Idempotency Filter: Deleted idempotency key {}", key);
            }

            // Always flush buffered body to the actual response.
            // If this is skipped the client receives an empty body.
            wrappedResponse.copyBodyToResponse();
        }

    }

    private void replay(HttpServletRequest request, HttpServletResponse response, String storedValue) throws IOException {
        int separatorIndex = storedValue.indexOf(SEPARATOR);
        if(separatorIndex < 0){
            var ex = new IdempotencyConflictException("Request is in progress");
            //TODO: Handle exception properly in global exception handler
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

        int status = Integer.parseInt(storedValue.substring(0, separatorIndex));
        String body = storedValue.substring(separatorIndex + SEPARATOR.length());

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
