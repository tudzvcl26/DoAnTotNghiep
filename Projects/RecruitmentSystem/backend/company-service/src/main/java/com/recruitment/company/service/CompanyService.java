package com.recruitment.company.service;

import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;

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

}
