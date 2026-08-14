package com.recruitment.company.service;

import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.VerificationStatus;

public interface CompanyService {

    CompanyResponse create(CreateCompanyRequest request);

    CompanyResponse update(
            UUID companyId,
            UpdateCompanyRequest request
    );

    CompanyResponse getById(UUID companyId);

    CompanyResponse getBySlug(String slug);

    List<CompanyResponse> getByOwnerId(UUID ownerId);

    Page<CompanyResponse> getAll(Pageable pageable);

    Page<CompanyResponse> search(
            String keyword,
            Pageable pageable
    );

    void delete(UUID companyId);

    Page<CompanyResponse> getAdminCompanies(String keyword, CompanyStatus status,
                                            VerificationStatus verificationStatus, UUID ownerId, Pageable pageable);

    CompanyResponse getAdminCompany(UUID companyId);

    CompanyResponse updateVerification(UUID companyId, VerificationStatus verificationStatus);

}
