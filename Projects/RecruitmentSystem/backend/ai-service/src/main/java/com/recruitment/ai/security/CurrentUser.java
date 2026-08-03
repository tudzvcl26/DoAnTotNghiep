package com.recruitment.ai.security;

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
        String target = normalize(role);
        return roles.stream().map(this::normalize).anyMatch(target::equalsIgnoreCase);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    private String normalize(String role) {
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

}
