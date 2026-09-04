package com.recruitment.ai.repository;

import com.recruitment.ai.entity.JobMatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface JobMatchResultRepository extends JpaRepository<JobMatchResult, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from JobMatchResult m where m.id = :id")
    Optional<JobMatchResult> lockById(@org.springframework.data.repository.query.Param("id") UUID id);
    @EntityGraph(attributePaths = {"breakdowns", "resumeAnalysisResult", "resumeAnalysisResult.resumeDocument"})
    Optional<JobMatchResult> findDetailedById(UUID id);
    Optional<JobMatchResult> findByJobIdAndResumeAnalysisResultId(UUID jobId, UUID resumeAnalysisResultId);
    Page<JobMatchResult> findByJobId(UUID jobId, Pageable pageable);
    Page<JobMatchResult> findByJobIdAndResumeOwnerUserId(UUID jobId, UUID ownerUserId, Pageable pageable);
    Page<JobMatchResult> findByResumeDocumentId(UUID resumeDocumentId, Pageable pageable);
    Page<JobMatchResult> findByResumeDocumentIdAndJobOwnerUserId(UUID resumeDocumentId, UUID ownerUserId, Pageable pageable);
}
