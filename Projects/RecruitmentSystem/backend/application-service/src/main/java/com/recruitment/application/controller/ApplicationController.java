package com.recruitment.application.controller;

import com.recruitment.application.common.ApiResponse;
import com.recruitment.application.common.PageResponse;
import com.recruitment.application.dto.request.ApplyJobRequest;
import com.recruitment.application.dto.request.UpdateApplicationStatusRequest;
import com.recruitment.application.dto.request.WithdrawApplicationRequest;
import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.dto.response.ApplicationResumeDownload;
import com.recruitment.application.dto.response.EmployerApplicationStatisticsResponse;
import com.recruitment.application.entity.enums.ApplicationStatus;
import com.recruitment.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Application", description = "Application Lifecycle Management APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/employer/statistics")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get owner-scoped employer application statistics")
    public ApiResponse<EmployerApplicationStatisticsResponse> getEmployerStatistics() {
        return ApiResponse.success(applicationService.getEmployerStatistics());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @Operation(summary = "Candidate apply for a job")
    public ApiResponse<ApplicationResponse> apply(
            @Valid
            @RequestBody
            ApplyJobRequest request
    ) {
        return ApiResponse.success(
                applicationService.apply(request)
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @Operation(summary = "Candidate get own application list")
    public ApiResponse<PageResponse<ApplicationSummaryResponse>> getMyApplications(
            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponse.success(
                applicationService.getMyApplications(pageable)
        );
    }

    @GetMapping("/employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Employer get applications across owned companies")
    public ApiResponse<PageResponse<ApplicationSummaryResponse>> getEmployerApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) UUID jobId,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(applicationService.getEmployerApplications(status, jobId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get application detail by ID")
    public ApiResponse<ApplicationResponse> getById(
            @Parameter(description = "Application ID")
            @PathVariable
            UUID id
    ) {
        return ApiResponse.success(
                applicationService.getById(id)
        );
    }

    @GetMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download the immutable resume snapshot for an application")
    public ResponseEntity<byte[]> downloadResume(@PathVariable UUID id) {
        ApplicationResumeDownload download = applicationService.downloadResume(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(download.contentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.filename(), StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(mediaType)
                .body(download.content());
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    @Operation(summary = "Candidate withdraw application")
    public ApiResponse<ApplicationResponse> withdraw(
            @Parameter(description = "Application ID")
            @PathVariable
            UUID id,

            @Valid
            @RequestBody(required = false)
            WithdrawApplicationRequest request
    ) {
        return ApiResponse.success(
                applicationService.withdraw(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Employer/Admin update application status")
    public ApiResponse<ApplicationResponse> updateStatus(
            @Parameter(description = "Application ID")
            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateApplicationStatusRequest request
    ) {
        return ApiResponse.success(
                applicationService.updateStatus(id, request)
        );
    }

}
