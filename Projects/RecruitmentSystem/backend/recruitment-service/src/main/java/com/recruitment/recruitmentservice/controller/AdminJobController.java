package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import com.recruitment.recruitmentservice.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Job Moderation", description = "Admin-only job moderation APIs")
public class AdminJobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "List jobs across all companies for moderation")
    public ApiResponse<PageResponse<JobSummaryResponse>> getJobs(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(jobService.getEmployerJobs(companyId, status, keyword, pageable));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "View a job for moderation")
    public ApiResponse<JobResponse> getJob(@PathVariable UUID jobId) {
        return ApiResponse.success(jobService.getById(jobId));
    }

    @PatchMapping("/{jobId}/publish")
    @Operation(summary = "Publish a draft job")
    public ApiResponse<JobResponse> publish(@PathVariable UUID jobId) {
        return ApiResponse.success(jobService.publish(jobId));
    }

    @PatchMapping("/{jobId}/close")
    @Operation(summary = "Close a published job")
    public ApiResponse<JobResponse> close(@PathVariable UUID jobId) {
        return ApiResponse.success(jobService.close(jobId));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Deactivate a job")
    public ApiResponse<Void> delete(@PathVariable UUID jobId) {
        jobService.delete(jobId);
        return ApiResponse.success();
    }
}
