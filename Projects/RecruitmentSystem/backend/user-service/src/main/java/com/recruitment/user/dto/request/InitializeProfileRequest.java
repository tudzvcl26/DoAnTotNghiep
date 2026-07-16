package com.recruitment.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitializeProfileRequest {

    @NotBlank(message = "Display name is required")
    private String displayName;

}