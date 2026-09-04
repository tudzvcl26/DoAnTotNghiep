package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ResumeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResumeDocumentRepository extends JpaRepository<ResumeDocument, UUID> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from ResumeDocument d where d.id = :id and d.deletedAt is null")
    Optional<ResumeDocument> lockActiveById(@org.springframework.data.repository.query.Param("id") UUID id);

    Page<ResumeDocument> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

    Page<ResumeDocument> findAllByDeletedAtIsNull(Pageable pageable);

    Page<ResumeDocument> findByOwnerUserIdAndDeletedAtIsNull(UUID ownerUserId, Pageable pageable);

    Optional<ResumeDocument> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    Optional<ResumeDocument> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ResumeDocument> findByIdAndOwnerUserIdAndDeletedAtIsNull(UUID id, UUID ownerUserId);
}
