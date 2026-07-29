package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    boolean existsByJobCode(String jobCode);

    boolean existsByJobCodeAndIdNot(
            String jobCode,
            UUID id
    );

    Optional<Job> findByIdAndActiveTrue(UUID id);

    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByActiveTrueAndTitleContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    boolean existsByCategory_Id(UUID categoryId);

}
