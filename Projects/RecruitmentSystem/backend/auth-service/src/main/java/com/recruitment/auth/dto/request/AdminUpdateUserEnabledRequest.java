package com.recruitment.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserEnabledRequest {
    @NotNull(message = "Enabled is required.")
    private Boolean enabled;
}
