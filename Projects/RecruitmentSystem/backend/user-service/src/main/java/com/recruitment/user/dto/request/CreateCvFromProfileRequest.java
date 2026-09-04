package com.recruitment.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCvFromProfileRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Pattern(regexp = com.recruitment.user.dto.cv.CvTemplateCatalog.ID_PATTERN) String templateId
) {}
