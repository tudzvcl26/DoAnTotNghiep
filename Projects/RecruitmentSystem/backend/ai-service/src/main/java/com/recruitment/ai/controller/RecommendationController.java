package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.CandidateRecommendationResponse;
import com.recruitment.ai.dto.response.JobRecommendationResponse;
import com.recruitment.ai.dto.response.AiTaskResponse;
import com.recruitment.ai.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Deterministically ranked jobs and candidates with AI explanations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Rank published jobs for an analyzed candidate resume")
    public ApiResponse<PageResponse<JobRecommendationResponse>> jobs(
            @RequestParam(required = false) UUID resumeId,
            @RequestParam(defaultValue = "0") int minScore,
            @RequestParam(defaultValue = "100") int maxScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "overallScore") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ApiResponse.success(recommendationService.recommendJobs(
                resumeId, minScore, maxScore, page(page, size, sort, direction)));
    }

    @PostMapping("/jobs/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Queue an asynchronous refresh of candidate job recommendations")
    public ApiResponse<AiTaskResponse> refreshJobs(@RequestParam(required = false) UUID resumeId) {
        return ApiResponse.success(recommendationService.refreshJobs(resumeId));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Get one owned job recommendation")
    public ApiResponse<JobRecommendationResponse> job(@PathVariable UUID id) {
        return ApiResponse.success(recommendationService.getJobRecommendation(id));
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    @Operation(summary = "Rank analyzed candidates for one owned published job")
    public ApiResponse<PageResponse<CandidateRecommendationResponse>> candidates(
            @RequestParam UUID jobId,
            @RequestParam(defaultValue = "0") int minScore,
            @RequestParam(defaultValue = "100") int maxScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "overallScore") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ApiResponse.success(recommendationService.recommendCandidates(
                jobId, minScore, maxScore, page(page, size, sort, direction)));
    }

    @GetMapping("/candidates/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    @Operation(summary = "Get one candidate recommendation for an owned job")
    public ApiResponse<CandidateRecommendationResponse> candidate(@PathVariable UUID id) {
        return ApiResponse.success(recommendationService.getCandidateRecommendation(id));
    }

    private PageRequest page(int page, int size, String sort, String direction) {
        String property = "updatedAt".equals(sort) ? "updatedAt" : "overallScore";
        Sort.Direction order = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by(order, property));
    }
}
