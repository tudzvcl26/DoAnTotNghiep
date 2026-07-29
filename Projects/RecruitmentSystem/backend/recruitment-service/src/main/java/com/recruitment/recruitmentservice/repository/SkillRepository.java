package com.recruitment.recruitmentservice.repository;

import com.recruitment.recruitmentservice.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findBySlug(String slug);

    Optional<Skill> findByNameIgnoreCase(String name);

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

    Optional<Skill> findByIdAndActiveTrue(UUID id);

    Page<Skill> findByActiveTrue(Pageable pageable);

    Page<Skill> findByActiveTrueAndNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

}
