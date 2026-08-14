package com.recruitment.application.controller;

import com.recruitment.application.common.ApiResponse;
import com.recruitment.application.common.PageResponse;
import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.entity.enums.ApplicationStatus;
import com.recruitment.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApplicationController {
    private final ApplicationService applicationService;

    @GetMapping
    public ApiResponse<PageResponse<ApplicationSummaryResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID candidateId,
            Pageable pageable) {
        return ApiResponse.success(applicationService.getAdminApplications(status, jobId, companyId, candidateId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationResponse> getApplication(@PathVariable UUID id) {
        return ApiResponse.success(applicationService.getById(id));
    }
}
