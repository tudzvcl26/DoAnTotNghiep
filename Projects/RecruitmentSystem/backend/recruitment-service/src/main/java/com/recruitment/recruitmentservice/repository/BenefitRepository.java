package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.Benefit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BenefitRepository extends JpaRepository<Benefit, UUID> {

    Optional<Benefit> findBySlug(String slug);

    Optional<Benefit> findByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    boolean existsBySlugAndIdNot(
            String slug,
            UUID id
    );

    Optional<Benefit> findByIdAndActiveTrue(UUID id);

    Page<Benefit> findByActiveTrue(Pageable pageable);

    Page<Benefit> findByActiveTrueAndNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

}
