package com.recruitment.user.repository;

import com.recruitment.user.entity.UserSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID>,
        JpaSpecificationExecutor<UserSkill> {

    Page<UserSkill> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<UserSkill> findByIdAndDeletedAtIsNull(
            UUID id
    );

    boolean existsByProfileIdAndSkillIdAndDeletedAtIsNull(
            UUID profileId,
            UUID skillId
    );

}