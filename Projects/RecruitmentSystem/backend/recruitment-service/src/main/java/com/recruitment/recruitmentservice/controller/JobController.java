package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSearchRequest;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.EmployerJobStatisticsResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import com.recruitment.recruitmentservice.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "Job Management APIs")
public class JobController {

    private final JobService jobService;

    @GetMapping("/recommendation-feed")
    @Operation(summary = "Get paged published job details for recommendation processing")
    public ApiResponse<PageResponse<JobResponse>> getRecommendationFeed(@ParameterObject Pageable pageable) {
        return ApiResponse.success(jobService.getRecommendationFeed(pageable));
    }

    @GetMapping("/employer/statistics")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get owner-scoped employer job statistics")
    public ApiResponse<EmployerJobStatisticsResponse> getEmployerStatistics() {
        return ApiResponse.success(jobService.getEmployerStatistics());
    }

    @GetMapping("/employer")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get owner-scoped employer jobs")
    public ApiResponse<PageResponse<JobSummaryResponse>> getEmployerJobs(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(jobService.getEmployerJobs(companyId, status, keyword, pageable));
    }

    @GetMapping("/employer/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get an owner-scoped employer job by id")
    public ApiResponse<JobResponse> getEmployerJob(
            @Parameter(description = "Job ID") @PathVariable UUID id
    ) {
        return ApiResponse.success(jobService.getEmployerJob(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Create new job")
    public ApiResponse<JobResponse> create(

            @Valid
            @RequestBody
            CreateJobRequest request
    ) {

        return ApiResponse.success(
                jobService.create(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Update job")
    public ApiResponse<JobResponse> update(

            @Parameter(description = "Job ID")
            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateJobRequest request
    ) {

        return ApiResponse.success(
                jobService.update(id, request)
        );
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Publish job")
    public ApiResponse<JobResponse> publish(

            @Parameter(description = "Job ID")
            @PathVariable
            UUID id
    ) {

        return ApiResponse.success(
                jobService.publish(id)
        );
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Close job")
    public ApiResponse<JobResponse> close(

            @Parameter(description = "Job ID")
            @PathVariable
            UUID id
    ) {

        return ApiResponse.success(
                jobService.close(id)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Delete job")
    public ApiResponse<Void> delete(

            @Parameter(description = "Job ID")
            @PathVariable
            UUID id
    ) {

        jobService.delete(id);

        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public ApiResponse<JobResponse> getById(

            @Parameter(description = "Job ID")
            @PathVariable
            UUID id
    ) {

        return ApiResponse.success(
                jobService.getById(id)
        );
    }

    @GetMapping
    @Operation(summary = "Get all jobs")
    public ApiResponse<PageResponse<JobSummaryResponse>> getAll(

            @ParameterObject
            Pageable pageable
    ) {

        return ApiResponse.success(
                jobService.getAll(pageable)
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter jobs")
    public ApiResponse<PageResponse<JobSummaryResponse>> search(

            @Valid
            @ParameterObject
            JobSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {

        return ApiResponse.success(
                jobService.search(
                        request,
                        pageable
                )
        );
    }

    @GetMapping("/public-search")
    @Operation(summary = "Search published jobs for the public job seeker experience")
    public ApiResponse<PageResponse<JobSummaryResponse>> publicSearch(

            @Valid
            @ParameterObject
            JobSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponse.success(jobService.publicSearch(request, pageable));
    }

}
