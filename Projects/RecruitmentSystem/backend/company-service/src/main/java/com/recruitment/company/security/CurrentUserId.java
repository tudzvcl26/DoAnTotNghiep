package com.recruitment.company.security;

import java.util.UUID;

public final class CurrentUserId {

    private CurrentUserId() {
    }

    public static UUID get() {

        CurrentUser currentUser = SecurityUtils.getCurrentUser();

        if (currentUser == null) {
            throw new IllegalStateException("User is not authenticated.");
        }

        return currentUser.getUserId();

    }

}
