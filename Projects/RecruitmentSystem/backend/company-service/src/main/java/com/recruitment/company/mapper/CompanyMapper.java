package com.recruitment.company.mapper;

import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import com.recruitment.company.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CreateCompanyRequest request) {

        if (request == null) {
            return null;
        }

        return Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .email(request.getEmail())
                .phone(request.getPhone())
                .taxCode(request.getTaxCode())
                .companyType(request.getCompanyType())
                .companySize(request.getCompanySize())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .build();

    }

    public void updateEntity(
            Company company,
            UpdateCompanyRequest request
    ) {

        if (company == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            company.setName(request.getName());
        }

        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }

        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }

        if (request.getEmail() != null) {
            company.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }

        if (request.getCompanyType() != null) {
            company.setCompanyType(request.getCompanyType());
        }

        if (request.getCompanySize() != null) {
            company.setCompanySize(request.getCompanySize());
        }

        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }

        if (request.getBannerUrl() != null) {
            company.setBannerUrl(request.getBannerUrl());
        }

    }

    public CompanyResponse toResponse(Company company) {

        if (company == null) {
            return null;
        }

        return CompanyResponse.builder()
                .id(company.getId())
                .ownerId(company.getOwnerId())
                .name(company.getName())
                .slug(company.getSlug())
                .description(company.getDescription())
                .website(company.getWebsite())
                .email(company.getEmail())
                .phone(company.getPhone())
                .taxCode(company.getTaxCode())
                .companyType(company.getCompanyType())
                .companySize(company.getCompanySize())
                .verificationStatus(company.getVerificationStatus())
                .status(company.getStatus())
                .logoUrl(company.getLogoUrl())
                .bannerUrl(company.getBannerUrl())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();

    }

}