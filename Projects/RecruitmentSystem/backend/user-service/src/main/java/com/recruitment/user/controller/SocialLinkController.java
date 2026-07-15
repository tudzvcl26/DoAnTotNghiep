package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateSocialLinkRequest;
import com.recruitment.user.dto.request.UpdateSocialLinkRequest;
import com.recruitment.user.dto.response.SocialLinkResponse;
import com.recruitment.user.service.SocialLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/social-links")
@RequiredArgsConstructor
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    @GetMapping
    public ApiResponse<Page<SocialLinkResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Social links retrieved successfully",
                socialLinkService.getAll(userId, pageable),
                request.getRequestURI()
        );

    }

    @GetMapping("/{socialLinkId}")
    public ApiResponse<SocialLinkResponse> getById(
            @PathVariable UUID socialLinkId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Social link retrieved successfully",
                socialLinkService.getById(socialLinkId),
                request.getRequestURI()
        );

    }

    @PostMapping
    public ApiResponse<SocialLinkResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateSocialLinkRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Social link created successfully",
                socialLinkService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping("/{socialLinkId}")
    public ApiResponse<SocialLinkResponse> update(
            @PathVariable UUID socialLinkId,
            @Valid @RequestBody UpdateSocialLinkRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Social link updated successfully",
                socialLinkService.update(socialLinkId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/{socialLinkId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID socialLinkId,
            HttpServletRequest request
    ) {

        socialLinkService.delete(socialLinkId);

        return ApiResponse.success(
                "Social link deleted successfully",
                request.getRequestURI()
        );

    }

}