package com.recruitment.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_certificates_profile_id", columnList = "profile_id")
})
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @NotBlank
    @Size(max = 255)
    @Column(name = "certificate_name", nullable = false, length = 255)
    private String certificateName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "issuer_name", nullable = false, length = 255)
    private String issuerName;

    @Size(max = 150)
    @Column(name = "credential_id", length = 150)
    private String credentialId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Size(max = 2048)
    @Column(name = "verification_url", length = 2048)
    private String verificationUrl;

}
