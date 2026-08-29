package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.MatchingResultResponse;
import com.recruitment.ai.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/matching")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER','ADMIN')")
@Tag(name = "Rule-based Matching", description = "Deterministic resume-to-job matching APIs")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/jobs/{jobId}/resumes/{resumeId}")
    @Operation(summary = "Calculate and persist a deterministic job match")
    public ApiResponse<MatchingResultResponse> match(@PathVariable UUID jobId, @PathVariable UUID resumeId) {
        return ApiResponse.success("Đã đánh giá độ phù hợp thành công.", matchingService.match(jobId, resumeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a matching result")
    public ApiResponse<MatchingResultResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(matchingService.getById(id));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "List matching results visible for a job")
    public ApiResponse<PageResponse<MatchingResultResponse>> getByJob(
            @PathVariable UUID jobId, @ParameterObject Pageable pageable) {
        return ApiResponse.success(matchingService.getByJob(jobId, pageable));
    }

    @GetMapping("/resume/{resumeId}")
    @Operation(summary = "List matching results visible for a resume")
    public ApiResponse<PageResponse<MatchingResultResponse>> getByResume(
            @PathVariable UUID resumeId, @ParameterObject Pageable pageable) {
        return ApiResponse.success(matchingService.getByResume(resumeId, pageable));
    }
}
