package com.recruitment.company.dto.response;

import com.recruitment.company.enums.CompanySize;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.CompanyType;
import com.recruitment.company.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private UUID id;

    private UUID ownerId;

    private String name;

    private String slug;

    private String description;

    private String website;

    private String email;

    private String phone;

    private String taxCode;

    private CompanyType companyType;

    private CompanySize companySize;

    private VerificationStatus verificationStatus;

    private CompanyStatus status;

    private String logoUrl;

    private String bannerUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}