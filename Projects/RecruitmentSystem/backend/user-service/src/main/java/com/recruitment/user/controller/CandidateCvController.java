package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateCvFromProfileRequest;
import com.recruitment.user.dto.request.SaveCandidateCvRequest;
import com.recruitment.user.dto.response.CandidateCvResponse;
import com.recruitment.user.security.CurrentUserId;
import com.recruitment.user.security.SecurityUtils;
import com.recruitment.user.service.CandidateCvService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cvs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateCvController {

    private final CandidateCvService service;

    @GetMapping
    public ApiResponse<List<CandidateCvResponse>> list(HttpServletRequest request) {
        return ApiResponse.success("CVs retrieved successfully", service.list(CurrentUserId.get()), request.getRequestURI());
    }

    @GetMapping("/{cvId}")
    public ApiResponse<CandidateCvResponse> get(@PathVariable UUID cvId, HttpServletRequest request) {
        return ApiResponse.success("CV retrieved successfully", service.get(CurrentUserId.get(), cvId), request.getRequestURI());
    }

    @PostMapping
    public ApiResponse<CandidateCvResponse> create(@Valid @RequestBody SaveCandidateCvRequest body,
                                                    HttpServletRequest request) {
        return ApiResponse.success("CV created successfully", service.create(CurrentUserId.get(), body), request.getRequestURI());
    }

    @PostMapping("/from-profile")
    public ApiResponse<CandidateCvResponse> createFromProfile(
            @Valid @RequestBody CreateCvFromProfileRequest body, HttpServletRequest request) {
        String email = SecurityUtils.getCurrentUser() == null ? "" : SecurityUtils.getCurrentUser().getEmail();
        return ApiResponse.success("CV created from profile successfully",
                service.createFromProfile(CurrentUserId.get(), email, body), request.getRequestURI());
    }

    @PutMapping("/{cvId}")
    public ApiResponse<CandidateCvResponse> update(@PathVariable UUID cvId,
                                                    @Valid @RequestBody SaveCandidateCvRequest body,
                                                    HttpServletRequest request) {
        return ApiResponse.success("CV updated successfully",
                service.update(CurrentUserId.get(), cvId, body), request.getRequestURI());
    }

    @DeleteMapping("/{cvId}")
    public ApiResponse<Void> delete(@PathVariable UUID cvId, HttpServletRequest request) {
        service.delete(CurrentUserId.get(), cvId);
        return ApiResponse.success("CV deleted successfully", null, request.getRequestURI());
    }

    @GetMapping(value = "/{cvId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable UUID cvId) {
        byte[] pdf = service.download(CurrentUserId.get(), cvId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("cv-" + cvId + ".pdf", StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(pdf.length)
                .body(pdf);
    }
}
