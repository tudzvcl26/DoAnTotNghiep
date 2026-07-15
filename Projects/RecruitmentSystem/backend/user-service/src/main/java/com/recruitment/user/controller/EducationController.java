package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateEducationRequest;
import com.recruitment.user.dto.request.UpdateEducationRequest;
import com.recruitment.user.dto.response.EducationResponse;
import com.recruitment.user.service.EducationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/educations")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ApiResponse<Page<EducationResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Educations retrieved successfully",
                educationService.getAll(userId, pageable),
                request.getRequestURI()
        );
    }

    @GetMapping("/{educationId}")
    public ApiResponse<EducationResponse> getById(
            @PathVariable UUID educationId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Education retrieved successfully",
                educationService.getById(educationId),
                request.getRequestURI()
        );
    }

    @PostMapping
    public ApiResponse<EducationResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateEducationRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Education created successfully",
                educationService.create(userId, body),
                request.getRequestURI()
        );
    }

    @PutMapping("/{educationId}")
    public ApiResponse<EducationResponse> update(
            @PathVariable UUID educationId,
            @Valid @RequestBody UpdateEducationRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Education updated successfully",
                educationService.update(educationId, body),
                request.getRequestURI()
        );
    }

    @DeleteMapping("/{educationId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID educationId,
            HttpServletRequest request
    ) {

        educationService.delete(educationId);

        return ApiResponse.success(
                "Education deleted successfully",
                request.getRequestURI()
        );
    }
}