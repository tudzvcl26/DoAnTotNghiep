package com.recruitment.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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

                filterChain.doFilter(request, response);
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
                            token
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

        }

        filterChain.doFilter(request, response);

    }

}