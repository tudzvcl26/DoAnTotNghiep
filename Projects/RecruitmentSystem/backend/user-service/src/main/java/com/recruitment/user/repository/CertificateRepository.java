package com.recruitment.user.repository;

import com.recruitment.user.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID>,
        JpaSpecificationExecutor<Certificate> {

    Page<Certificate> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<Certificate> findByIdAndDeletedAtIsNull(
            UUID id
    );

    boolean existsByProfileIdAndCertificateNameAndIssuerNameAndIssueDateAndDeletedAtIsNull(
            UUID profileId,
            String certificateName,
            String issuerName,
            LocalDate issueDate
    );

}