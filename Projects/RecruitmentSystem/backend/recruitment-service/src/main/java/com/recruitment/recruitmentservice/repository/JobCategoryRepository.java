package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.JobCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface JobCategoryRepository extends JpaRepository<JobCategory, UUID> {

    Optional<JobCategory> findBySlug(String slug);

    Optional<JobCategory> findByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    boolean existsByParent_Id(UUID parentId);

    Optional<JobCategory> findByIdAndActiveTrue(UUID id);

    @EntityGraph(attributePaths = "parent")
    Page<JobCategory> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "parent")
    Page<JobCategory> findByActiveTrueAndNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

}
