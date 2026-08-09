package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ResumeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeDocumentRepository extends JpaRepository<ResumeDocument, UUID> {

    Page<ResumeDocument> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

    Page<ResumeDocument> findAllByDeletedAtIsNull(Pageable pageable);

    Page<ResumeDocument> findByOwnerUserIdAndDeletedAtIsNull(UUID ownerUserId, Pageable pageable);

    Optional<ResumeDocument> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    Optional<ResumeDocument> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ResumeDocument> findByIdAndOwnerUserIdAndDeletedAtIsNull(UUID id, UUID ownerUserId);
}
