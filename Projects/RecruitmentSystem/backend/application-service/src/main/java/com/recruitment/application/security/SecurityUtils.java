package com.recruitment.application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CurrentUser currentUser) {
            return currentUser;
        }

        return null;
    }

    public static String getBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Object credentials = jwtAuthenticationToken.getCredentials();
            if (credentials instanceof String token) {
                return token.startsWith(SecurityConstants.TOKEN_PREFIX)
                        ? token
                        : SecurityConstants.TOKEN_PREFIX + token;
            }
        }

        return null;
    }

}
