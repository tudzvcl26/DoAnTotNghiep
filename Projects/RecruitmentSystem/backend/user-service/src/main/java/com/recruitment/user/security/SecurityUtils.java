package com.recruitment.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CurrentUser currentUser) {
            return currentUser;
        }

        return null;

    }

    public static UUID getCurrentUserId() {

        CurrentUser currentUser = getCurrentUser();

        if (currentUser == null) {
            return null;
        }

        return currentUser.getUserId();

    }

}