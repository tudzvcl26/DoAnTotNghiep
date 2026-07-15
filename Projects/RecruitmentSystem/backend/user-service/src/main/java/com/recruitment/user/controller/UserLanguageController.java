package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateLanguageRequest;
import com.recruitment.user.dto.request.UpdateLanguageRequest;
import com.recruitment.user.dto.response.LanguageResponse;
import com.recruitment.user.service.UserLanguageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/languages")
@RequiredArgsConstructor
public class UserLanguageController {

    private final UserLanguageService userLanguageService;

    @GetMapping
    public ApiResponse<Page<LanguageResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Languages retrieved successfully",
                userLanguageService.getAll(userId, pageable),
                request.getRequestURI()
        );

    }

    @GetMapping("/{userLanguageId}")
    public ApiResponse<LanguageResponse> getById(
            @PathVariable UUID userLanguageId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Language retrieved successfully",
                userLanguageService.getById(userLanguageId),
                request.getRequestURI()
        );

    }

    @PostMapping
    public ApiResponse<LanguageResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateLanguageRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Language created successfully",
                userLanguageService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping("/{userLanguageId}")
    public ApiResponse<LanguageResponse> update(
            @PathVariable UUID userLanguageId,
            @Valid @RequestBody UpdateLanguageRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Language updated successfully",
                userLanguageService.update(userLanguageId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/{userLanguageId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID userLanguageId,
            HttpServletRequest request
    ) {

        userLanguageService.delete(userLanguageId);

        return ApiResponse.success(
                "Language deleted successfully",
                request.getRequestURI()
        );

    }

}