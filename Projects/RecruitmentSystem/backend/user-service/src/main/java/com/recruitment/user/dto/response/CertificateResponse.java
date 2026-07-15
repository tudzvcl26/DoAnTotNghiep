package com.recruitment.user.dto.response;
import lombok.Builder; import lombok.Getter; import java.time.LocalDate; import java.util.UUID;
@Getter @Builder public class CertificateResponse { private UUID id; private String certificateName; private String issuerName; private String credentialId; private LocalDate issueDate; private LocalDate expiryDate; private String verificationUrl; private Long version; }
