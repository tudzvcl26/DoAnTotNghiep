package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ResumeAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ResumeAnalysisResultRepository extends JpaRepository<ResumeAnalysisResult, UUID> {

    @EntityGraph(attributePaths = "resumeDocument")
    Optional<ResumeAnalysisResult> findByResumeDocumentId(UUID resumeDocumentId);

    @EntityGraph(attributePaths = "resumeDocument")
    Optional<ResumeAnalysisResult> findFirstByResumeDocumentOwnerUserIdOrderByUpdatedAtDesc(UUID ownerUserId);

    @EntityGraph(attributePaths = "resumeDocument")
    List<ResumeAnalysisResult> findAllByOrderByUpdatedAtDesc();
}
