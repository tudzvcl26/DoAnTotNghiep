package com.recruitment.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCvFromProfileRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Pattern(regexp = "classic|modern|ats|student|professional") String templateId
) {}
