package com.recruitment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {
    @NotBlank(message = "Verification token is required.")
    @Size(max = 128, message = "Verification token is invalid.")
    private String token;
}
