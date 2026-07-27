package com.recruitment.company.entity;

import com.recruitment.company.enums.CompanySize;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.CompanyType;
import com.recruitment.company.enums.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Company aggregate root.
 *
 * Represents an employer organization registered on the recruitment platform.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_slug",
                        columnNames = "slug"
                ),
                @UniqueConstraint(
                        name = "uk_company_tax_code",
                        columnNames = "tax_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_company_owner",
                        columnList = "owner_id"
                ),
                @Index(
                        name = "idx_company_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_company_verification_status",
                        columnList = "verification_status"
                )
        }
)
public class Company extends BaseEntity {

    /**
     * User ID of the company owner.
     */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /**
     * Official company name.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * URL-friendly unique slug.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    /**
     * Company introduction.
     */
    @Size(max = 5000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Official website.
     */
    @Size(max = 255)
    @Column(name = "website", length = 255)
    private String website;

    /**
     * Company email.
     */
    @Email
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Contact phone number.
     */
    @Size(max = 50)
    @Column(name = "phone", length = 50)
    private String phone;

    /**
     * Tax identification number.
     */
    @Size(max = 100)
    @Column(name = "tax_code", length = 100)
    private String taxCode;

    /**
     * Company type.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false, length = 50)
    private CompanyType companyType;

    /**
     * Company size.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "company_size", nullable = false, length = 50)
    private CompanySize companySize;

    /**
     * Verification status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus;

    /**
     * Company status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CompanyStatus status;

    /**
     * Company logo URL.
     */
    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Company banner URL.
     */
    @Size(max = 500)
    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

}