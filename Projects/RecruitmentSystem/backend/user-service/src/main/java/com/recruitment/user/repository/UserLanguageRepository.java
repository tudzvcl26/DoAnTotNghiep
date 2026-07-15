package com.recruitment.user.repository;

import com.recruitment.user.entity.UserLanguage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserLanguageRepository extends JpaRepository<UserLanguage, UUID>,
        JpaSpecificationExecutor<UserLanguage> {

    Page<UserLanguage> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<UserLanguage> findByIdAndDeletedAtIsNull(
            UUID id
    );

    Optional<UserLanguage> findByProfileIdAndLanguageIdAndDeletedAtIsNull(
            UUID profileId,
            UUID languageId
    );

    boolean existsByProfileIdAndLanguageIdAndDeletedAtIsNull(
            UUID profileId,
            UUID languageId
    );

}