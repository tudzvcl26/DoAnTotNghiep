package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateExperienceRequest;
import com.recruitment.user.dto.request.UpdateExperienceRequest;
import com.recruitment.user.dto.response.ExperienceResponse;
import com.recruitment.user.service.ExperienceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public ApiResponse<Page<ExperienceResponse>> getAll(
            @PathVariable UUID userId,
            Pageable pageable,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Experience list retrieved successfully",
                experienceService.getAll(userId, pageable),
                request.getRequestURI()
        );

    }

    @GetMapping("/{experienceId}")
    public ApiResponse<ExperienceResponse> getById(
            @PathVariable UUID experienceId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Experience retrieved successfully",
                experienceService.getById(experienceId),
                request.getRequestURI()
        );

    }

    @PostMapping
    public ApiResponse<ExperienceResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateExperienceRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Experience created successfully",
                experienceService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping("/{experienceId}")
    public ApiResponse<ExperienceResponse> update(
            @PathVariable UUID experienceId,
            @Valid @RequestBody UpdateExperienceRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Experience updated successfully",
                experienceService.update(experienceId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping("/{experienceId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID experienceId,
            HttpServletRequest request
    ) {

        experienceService.delete(experienceId);

        return ApiResponse.success(
                "Experience deleted successfully",
                request.getRequestURI()
        );

    }

}