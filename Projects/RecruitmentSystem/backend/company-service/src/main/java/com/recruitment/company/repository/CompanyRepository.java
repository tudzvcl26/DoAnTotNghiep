package com.recruitment.company.repository;

import com.recruitment.company.entity.Company;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends
        JpaRepository<Company, UUID>,
        JpaSpecificationExecutor<Company> {

    Optional<Company> findByIdAndStatus(
            UUID id,
            CompanyStatus status
    );

    Optional<Company> findBySlug(String slug);

    Optional<Company> findByOwnerId(UUID ownerId);

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);

    boolean existsByTaxCode(String taxCode);

    boolean existsByName(String name);

    Page<Company> findAllByStatus(
            CompanyStatus status,
            Pageable pageable
    );

    Page<Company> findAllByVerificationStatus(
            VerificationStatus verificationStatus,
            Pageable pageable
    );

    Page<Company> findAllByOwnerId(
            UUID ownerId,
            Pageable pageable
    );

}