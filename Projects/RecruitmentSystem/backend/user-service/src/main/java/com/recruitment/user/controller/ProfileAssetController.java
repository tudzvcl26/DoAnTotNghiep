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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/assets")
@RequiredArgsConstructor
public class ProfileAssetController {

    private final ProfileAssetService profileAssetService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ProfileAssetResponse> upload(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("kind") ProfileAssetKind kind,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Asset uploaded successfully",
                profileAssetService.upload(
                        userId,
                        file,
                        kind
                ),
                request.getRequestURI()
        );

    }

    @GetMapping
    public ApiResponse<Page<ProfileAssetResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Assets retrieved successfully",
                profileAssetService.getAll(
                        userId,
                        pageable
                ),
                request.getRequestURI()
        );

    }

    @GetMapping("/{assetId}")
    public ApiResponse<ProfileAssetResponse> get(
            @PathVariable UUID assetId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Asset retrieved successfully",
                profileAssetService.getById(assetId),
                request.getRequestURI()
        );

    }

    @GetMapping("/avatar")
    public ApiResponse<ProfileAssetResponse> avatar(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Avatar retrieved successfully",
                profileAssetService.getAvatar(userId),
                request.getRequestURI()
        );

    }

    @GetMapping("/download/{assetId}")
    public ResponseEntity<byte[]> download(
            @PathVariable UUID assetId
    ) {

        byte[] data = profileAssetService.download(assetId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(assetId.toString())
                                .build()
                                .toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);

    }

    @DeleteMapping("/{assetId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID assetId,
            HttpServletRequest request
    ) {

        profileAssetService.delete(assetId);

        return ApiResponse.success(
                "Asset deleted successfully",
                request.getRequestURI()
        );

    }

}