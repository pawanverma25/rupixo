package dev.pawan.rupixo.merchant.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String BEARER = "Bearer ";

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming Request : {}", request.getRequestURI());

        try {

            final String authorizationHeader = request.getHeader("Authorization");
            if(authorizationHeader == null || !authorizationHeader.startsWith(BEARER)){
                filterChain.doFilter(request, response);
                return;
            }

            String jwtToken = authorizationHeader.substring(BEARER.length());
            Claims claims = jwtUtil.verifyAccessToken(jwtToken);

            if(claims != null && SecurityContextHolder.getContext().getAuthentication() == null){
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(jwtUtil.extractRole(claims)))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                merchantContext.setMerchantId(jwtUtil.extractMerchantId(claims));
            }
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
