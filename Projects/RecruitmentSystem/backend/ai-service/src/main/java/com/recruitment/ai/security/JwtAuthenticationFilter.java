package com.recruitment.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(SecurityConstants.BEARER_PREFIX.length());
        try {
            if (!jwtService.validateToken(token)) {
                reject(request, response, null);
                return;
            }
            CurrentUser currentUser = CurrentUser.builder()
                    .userId(UUID.fromString(jwtService.extractUserId(token)))
                    .email(jwtService.extractEmail(token))
                    .roles(Set.copyOf(jwtService.extractRoles(token)))
                    .build();
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                    currentUser,
                    token,
                    currentUser.getRoles().stream().map(this::authority).toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exception) {
            log.warn("JWT authentication failed: {}", exception.getClass().getSimpleName());
            reject(request, response, exception);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private SimpleGrantedAuthority authority(String role) {
        return new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role);
    }

    private void reject(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception cause
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        InsufficientAuthenticationException exception = cause == null
                ? new InsufficientAuthenticationException("Invalid JWT")
                : new InsufficientAuthenticationException("Invalid JWT", cause);
        authenticationEntryPoint.commence(request, response, exception);
    }

}
