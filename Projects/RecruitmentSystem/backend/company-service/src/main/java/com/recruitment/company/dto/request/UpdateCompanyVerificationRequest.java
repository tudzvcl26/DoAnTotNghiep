package com.recruitment.company.dto.request;

import com.recruitment.company.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCompanyVerificationRequest {
    @NotNull(message = "Verification status is required.")
    private VerificationStatus verificationStatus;
}
