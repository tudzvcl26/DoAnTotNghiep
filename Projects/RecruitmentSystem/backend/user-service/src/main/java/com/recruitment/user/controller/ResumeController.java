package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.response.ProfileAssetResponse;
import com.recruitment.user.entity.ProfileAssetKind;
import com.recruitment.user.service.ProfileAssetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/users/{userId}/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ProfileAssetService profileAssetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<ProfileAssetResponse> upload(@PathVariable UUID userId,
                                                    @RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) {
        return ApiResponse.success("Resume uploaded successfully",
                profileAssetService.upload(userId, file, ProfileAssetKind.RESUME), request.getRequestURI());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<Page<ProfileAssetResponse>> list(@PathVariable UUID userId,
                                                        Pageable pageable,
                                                        HttpServletRequest request) {
        return ApiResponse.success("Resumes retrieved successfully",
                profileAssetService.getResumes(userId, pageable), request.getRequestURI());
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<ProfileAssetResponse> current(@PathVariable UUID userId,
                                                     HttpServletRequest request) {
        return ApiResponse.success("Current resume retrieved successfully",
                profileAssetService.getCurrentResume(userId), request.getRequestURI());
    }

    @GetMapping("/{assetId}/download")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable UUID userId, @PathVariable UUID assetId) {
        ProfileAssetResponse asset = profileAssetService.getResumeById(userId, assetId);
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(asset.getOriginalFilename(), StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .body(profileAssetService.downloadResume(userId, assetId));
    }

    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable UUID userId, @PathVariable UUID assetId,
                                    HttpServletRequest request) {
        profileAssetService.deleteResume(userId, assetId);
        return ApiResponse.success("Resume deleted successfully", request.getRequestURI());
    }
}
