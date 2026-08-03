package com.recruitment.notification.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser implements Serializable {

    private UUID userId;
    private String email;
    private Set<String> roles;

    public boolean hasRole(String role) {
        if (roles == null || role == null) {
            return false;
        }
        String target = role.startsWith("ROLE_") ? role.substring(5) : role;
        return roles.stream().anyMatch(currentRole -> {
            String cleanRole = currentRole.startsWith("ROLE_") ? currentRole.substring(5) : currentRole;
            return cleanRole.equalsIgnoreCase(target);
        });
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

}
