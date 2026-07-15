package com.recruitment.user.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDate;
@Getter @Setter public class CreateCertificateRequest {
    @NotBlank @Size(max = 255) private String certificateName;
    @NotBlank @Size(max = 255) private String issuerName;
    @Size(max = 150) private String credentialId;
    private LocalDate issueDate; private LocalDate expiryDate;
    @Size(max = 2048) private String verificationUrl;
}
