package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateCertificateRequest;
import com.recruitment.user.dto.request.UpdateCertificateRequest;
import com.recruitment.user.dto.response.CertificateResponse;
import com.recruitment.user.service.CertificateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public ApiResponse<Page<CertificateResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Certificates retrieved successfully",
                certificateService.getAll(userId, pageable),
                request.getRequestURI()
        );

    }

    @GetMapping("/{certificateId}")
    public ApiResponse<CertificateResponse> getById(
            @PathVariable UUID certificateId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Certificate retrieved successfully",
                certificateService.getById(certificateId),
                request.getRequestURI()
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<CertificateResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateCertificateRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Certificate created successfully",
                certificateService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping("/{certificateId}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<CertificateResponse> update(
            @PathVariable UUID certificateId,
            @Valid @RequestBody UpdateCertificateRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Certificate updated successfully",
                certificateService.update(certificateId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/{certificateId}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable UUID certificateId,
            HttpServletRequest request
    ) {

        certificateService.delete(certificateId);

        return ApiResponse.success(
                "Certificate deleted successfully",
                request.getRequestURI()
        );

    }

}