package com.recruitment.ai.service;

import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.CandidateRecommendationResponse;
import com.recruitment.ai.dto.response.JobRecommendationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RecommendationService {
    PageResponse<JobRecommendationResponse> recommendJobs(UUID resumeId, int minimumScore, int maximumScore, Pageable pageable);
    JobRecommendationResponse getJobRecommendation(UUID id);
    PageResponse<CandidateRecommendationResponse> recommendCandidates(UUID jobId, int minimumScore, int maximumScore, Pageable pageable);
    CandidateRecommendationResponse getCandidateRecommendation(UUID id);
}
