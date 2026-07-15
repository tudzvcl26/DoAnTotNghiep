package com.recruitment.user.repository;

import com.recruitment.user.entity.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<Language, UUID>, JpaSpecificationExecutor<Language> {
    Optional<Language> findByLanguageCodeAndDeletedAtIsNull(String languageCode);
    boolean existsByLanguageCodeAndDeletedAtIsNull(String languageCode);
    Page<Language> findByDisplayNameContainingIgnoreCaseAndDeletedAtIsNull(String displayName, Pageable pageable);
}
