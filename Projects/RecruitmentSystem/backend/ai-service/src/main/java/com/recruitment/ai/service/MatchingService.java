package com.recruitment.ai.service;

import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.MatchingResultResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MatchingService {
    MatchingResultResponse match(UUID jobId, UUID resumeId);
    MatchingResultResponse getById(UUID id);
    PageResponse<MatchingResultResponse> getByJob(UUID jobId, Pageable pageable);
    PageResponse<MatchingResultResponse> getByResume(UUID resumeId, Pageable pageable);
}
