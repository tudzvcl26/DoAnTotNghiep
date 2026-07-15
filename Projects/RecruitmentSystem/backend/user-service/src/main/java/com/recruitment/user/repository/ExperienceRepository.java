package com.recruitment.user.repository;

import com.recruitment.user.entity.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID>,
        JpaSpecificationExecutor<Experience> {

    Page<Experience> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<Experience> findByIdAndDeletedAtIsNull(
            UUID id
    );

    boolean existsByProfileIdAndEmployerNameAndJobTitleAndCurrentTrueAndDeletedAtIsNull(
            UUID profileId,
            String employerName,
            String jobTitle
    );

}