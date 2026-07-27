package com.recruitment.company.service.impl;

import com.recruitment.company.dto.request.CreateCompanyRequest;
import com.recruitment.company.dto.request.UpdateCompanyRequest;
import com.recruitment.company.dto.response.CompanyResponse;
import com.recruitment.company.entity.Company;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.VerificationStatus;
import com.recruitment.company.exception.CompanyAlreadyExistsException;
import com.recruitment.company.exception.CompanyNotFoundException;
import com.recruitment.company.mapper.CompanyMapper;
import com.recruitment.company.repository.CompanyRepository;
import com.recruitment.company.security.CurrentUserId;
import com.recruitment.company.service.CompanyService;
import com.recruitment.company.specification.CompanySpecification;
import com.recruitment.company.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    private final CompanyMapper companyMapper;

    @Override
    public CompanyResponse create(CreateCompanyRequest request) {

        validateDuplicate(request);

        Company company = companyMapper.toEntity(request);

        company.setOwnerId(CurrentUserId.get());

        company.setSlug(generateUniqueSlug(request.getName()));

        company.setStatus(CompanyStatus.ACTIVE);

        company.setVerificationStatus(VerificationStatus.PENDING);

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    @Override
    public CompanyResponse update(
            UUID companyId,
            UpdateCompanyRequest request
    ) {

        Company company = companyRepository
                .findByIdAndStatus(
                        companyId,
                        CompanyStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new CompanyNotFoundException(
                                "Company not found."
                        )
                );

        String oldName = company.getName();

        if (request.getEmail() != null
                && !request.getEmail().equals(company.getEmail())
                && companyRepository.existsByEmail(request.getEmail())) {

            throw new CompanyAlreadyExistsException(
                    "Email already exists."
            );

        }

        companyMapper.updateEntity(company, request);

        if (request.getName() != null
                && !request.getName().equals(oldName)) {

            company.setSlug(
                    generateUniqueSlug(request.getName())
            );

        }

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);

    }    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getById(UUID companyId) {

        Company company = companyRepository
                .findByIdAndStatus(
                        companyId,
                        CompanyStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new CompanyNotFoundException(
                                "Company not found."
                        )
                );

        return companyMapper.toResponse(company);

    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getBySlug(String slug) {

        Company company = companyRepository
                .findBySlug(slug)
                .orElseThrow(() ->
                        new CompanyNotFoundException(
                                "Company not found."
                        )
                );

        if (company.getStatus() != CompanyStatus.ACTIVE) {

            throw new CompanyNotFoundException(
                    "Company not found."
            );

        }

        return companyMapper.toResponse(company);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAll(Pageable pageable) {

        return companyRepository
                .findAllByStatus(
                        CompanyStatus.ACTIVE,
                        pageable
                )
                .map(companyMapper::toResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> search(
            String keyword,
            Pageable pageable
    ) {

        Specification<Company> specification =
                CompanySpecification.search(
                        keyword,
                        CompanyStatus.ACTIVE,
                        null,
                        null
                );

        return companyRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(companyMapper::toResponse);

    }

    @Override
    public void delete(UUID companyId) {

        Company company = companyRepository
                .findByIdAndStatus(
                        companyId,
                        CompanyStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new CompanyNotFoundException(
                                "Company not found."
                        )
                );

        company.setStatus(
                CompanyStatus.INACTIVE
        );

        companyRepository.save(company);

    }
    private void validateDuplicate(
            CreateCompanyRequest request
    ) {

        if (request.getEmail() != null
                && companyRepository.existsByEmail(
                request.getEmail())) {

            throw new CompanyAlreadyExistsException(
                    "Email already exists."
            );

        }

        if (request.getTaxCode() != null
                && companyRepository.existsByTaxCode(
                request.getTaxCode())) {

            throw new CompanyAlreadyExistsException(
                    "Tax code already exists."
            );

        }

        if (request.getName() != null
                && companyRepository.existsByName(
                request.getName())) {

            throw new CompanyAlreadyExistsException(
                    "Company name already exists."
            );

        }

    }

    private String generateUniqueSlug(
            String companyName
    ) {

        String baseSlug = SlugUtils.toSlug(
                companyName
        );

        if (baseSlug.isBlank()) {
            throw new IllegalArgumentException(
                    "Company name is invalid."
            );
        }

        String slug = baseSlug;

        int index = 1;

        while (companyRepository.existsBySlug(slug)) {

            slug = baseSlug + "-" + index;

            index++;

        }

        return slug;

    }}