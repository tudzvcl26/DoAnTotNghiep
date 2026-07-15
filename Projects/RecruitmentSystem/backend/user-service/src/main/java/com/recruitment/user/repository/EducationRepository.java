package com.recruitment.user.repository;

import com.recruitment.user.entity.Education;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface EducationRepository extends JpaRepository<Education, UUID>,
        JpaSpecificationExecutor<Education> {

    Page<Education> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<Education> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByProfileIdAndInstitutionNameAndQualificationAndStartDateAndDeletedAtIsNull(
            UUID profileId,
            String institutionName,
            String qualification,
            LocalDate startDate
    );

}