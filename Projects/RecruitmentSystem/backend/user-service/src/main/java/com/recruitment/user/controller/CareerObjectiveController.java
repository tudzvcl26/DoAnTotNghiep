package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.UpdateCareerObjectiveRequest;
import com.recruitment.user.dto.response.CareerObjectiveResponse;
import com.recruitment.user.service.CareerObjectiveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/career-objective")
@RequiredArgsConstructor
public class CareerObjectiveController {

    private final CareerObjectiveService careerObjectiveService;

    @GetMapping
    public ApiResponse<CareerObjectiveResponse> get(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Career objective retrieved successfully",
                careerObjectiveService.get(userId),
                request.getRequestURI()
        );

    }

    @PutMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<CareerObjectiveResponse> upsert(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateCareerObjectiveRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Career objective updated successfully",
                careerObjectiveService.upsert(userId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {

        careerObjectiveService.delete(userId);

        return ApiResponse.success(
                "Career objective deleted successfully",
                request.getRequestURI()
        );

    }

}