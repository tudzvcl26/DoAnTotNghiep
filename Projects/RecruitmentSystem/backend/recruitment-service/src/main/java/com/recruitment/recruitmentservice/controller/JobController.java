package com.recruitment.recruitmentservice.controller;

import com.recruitment.recruitmentservice.common.ApiResponse;
import com.recruitment.recruitmentservice.common.PageResponse;
import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
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
    @Operation(summary = "Search jobs by keyword")
    public ApiResponse<PageResponse<JobSummaryResponse>> search(

            @Parameter(
                    description = "Keyword to search by job title"
            )
            @RequestParam(required = false)
            String keyword,

            @ParameterObject
            Pageable pageable
    ) {

        return ApiResponse.success(
                jobService.search(
                        keyword,
                        pageable
                )
        );
    }

}
