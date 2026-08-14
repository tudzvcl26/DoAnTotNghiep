package com.recruitment.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AdminUpdateUserRolesRequest {
    @NotEmpty(message = "At least one role is required.")
    private Set<String> roles;
}
