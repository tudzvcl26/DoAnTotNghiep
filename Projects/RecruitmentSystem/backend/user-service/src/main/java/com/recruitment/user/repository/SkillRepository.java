package com.recruitment.user.repository;

import com.recruitment.user.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {
    Optional<Skill> findByNormalizedSkillKeyAndDeletedAtIsNull(String normalizedSkillKey);
    boolean existsByNormalizedSkillKeyAndDeletedAtIsNull(String normalizedSkillKey);
    Page<Skill> findByDisplayNameContainingIgnoreCaseAndDeletedAtIsNull(String displayName, Pageable pageable);
}
