package com.recruitment.ai.repository;

import com.recruitment.ai.entity.JobRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, UUID> {
    @Override
    @EntityGraph(attributePaths = {"matchResult", "matchResult.breakdowns"})
    Optional<JobRecommendation> findById(UUID id);
    Optional<JobRecommendation> findByMatchResultId(UUID matchId);
    Page<JobRecommendation> findByCandidateUserIdAndResumeDocumentIdAndOverallScoreBetween(
            UUID candidateUserId, UUID resumeDocumentId, int minimumScore, int maximumScore, Pageable pageable);
}
