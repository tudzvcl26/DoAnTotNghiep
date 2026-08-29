package com.recruitment.user.dto.request;

import com.recruitment.user.dto.cv.CvDocument;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveCandidateCvRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Pattern(regexp = "classic|modern|ats|student|professional") String templateId,
        @NotBlank @Pattern(regexp = "vi|en") String language,
        @NotNull @Valid CvDocument content
) {}
