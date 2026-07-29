package com.recruitment.recruitmentservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(SecurityConstants.HEADER);

        if (!StringUtils.hasText(header)
                || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(SecurityConstants.TOKEN_PREFIX.length());

        try {

            if (!jwtService.validateToken(token)) {

                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new InsufficientAuthenticationException("Invalid JWT")
                );
                return;

            }

            CurrentUser currentUser = CurrentUser.builder()
                    .userId(
                            UUID.fromString(
                                    jwtService.extractUserId(token)
                            )
                    )
                    .email(
                            jwtService.extractEmail(token)
                    )
                    .roles(
                            Set.copyOf(
                                    jwtService.extractRoles(token)
                            )
                    )
                    .build();

            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(
                            currentUser,
                            token,
                            currentUser.getRoles().stream()
                                    .map(this::toGrantedAuthority)
                                    .toList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception ex) {

            log.warn(
                    "JWT authentication failed: {}",
                    ex.getClass().getSimpleName()
            );

            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("Invalid JWT", ex)
            );

            return;

        }

        filterChain.doFilter(request, response);

    }

    private SimpleGrantedAuthority toGrantedAuthority(String role) {

        String authority = role.startsWith("ROLE_")
                ? role
                : "ROLE_" + role;

        return new SimpleGrantedAuthority(authority);
    }

}
