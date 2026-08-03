package com.recruitment.application.controller;

import com.recruitment.application.common.ApiResponse;
import com.recruitment.application.common.PageResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.entity.enums.ApplicationStatus;
import com.recruitment.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Application", description = "Job Applications Management APIs")
public class JobApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Company get application list for a job")
    public ApiResponse<PageResponse<ApplicationSummaryResponse>> getJobApplications(
            @Parameter(description = "Job ID")
            @PathVariable
            UUID jobId,

            @Parameter(description = "Optional status filter")
            @RequestParam(required = false)
            ApplicationStatus status,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponse.success(
                applicationService.getJobApplications(jobId, status, pageable)
        );
    }

}
