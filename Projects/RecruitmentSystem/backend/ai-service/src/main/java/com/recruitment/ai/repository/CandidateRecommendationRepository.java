package com.recruitment.ai.repository;

import com.recruitment.ai.entity.CandidateRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRecommendationRepository extends JpaRepository<CandidateRecommendation, UUID> {
    @Override
    @EntityGraph(attributePaths = {"matchResult", "matchResult.breakdowns"})
    Optional<CandidateRecommendation> findById(UUID id);
    Optional<CandidateRecommendation> findByMatchResultId(UUID matchId);
    Page<CandidateRecommendation> findByJobIdAndOverallScoreBetween(
            UUID jobId, int minimumScore, int maximumScore, Pageable pageable);
}
