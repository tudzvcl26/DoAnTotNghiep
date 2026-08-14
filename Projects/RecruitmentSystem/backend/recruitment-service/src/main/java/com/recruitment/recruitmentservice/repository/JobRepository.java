package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    boolean existsByJobCode(String jobCode);

    boolean existsByJobCodeAndIdNot(
            String jobCode,
            UUID id
    );

    Optional<Job> findByIdAndActiveTrue(UUID id);

    Optional<Job> findByIdAndActiveTrueAndStatus(
            UUID id,
            JobStatus status
    );

    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByActiveTrueAndStatus(
            JobStatus status,
            Pageable pageable
    );

    Page<Job> findByActiveTrueAndTitleContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<Job> findByActiveTrueAndStatusAndTitleContainingIgnoreCase(
            JobStatus status,
            String keyword,
            Pageable pageable
    );

    boolean existsByCategory_Id(UUID categoryId);

}
