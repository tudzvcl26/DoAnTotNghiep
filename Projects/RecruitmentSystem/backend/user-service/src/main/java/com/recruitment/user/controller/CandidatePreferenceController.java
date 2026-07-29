package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import com.recruitment.user.dto.request.CreateCandidatePreferenceRequest;
import com.recruitment.user.dto.request.UpdateCandidatePreferenceRequest;
import com.recruitment.user.dto.response.CandidatePreferenceResponse;
import com.recruitment.user.service.CandidatePreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/candidate-preference")
@RequiredArgsConstructor
public class CandidatePreferenceController {

    private final CandidatePreferenceService candidatePreferenceService;

    @GetMapping
    public ApiResponse<CandidatePreferenceResponse> get(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Candidate preference retrieved successfully",
                candidatePreferenceService.get(userId),
                request.getRequestURI()
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<CandidatePreferenceResponse> create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateCandidatePreferenceRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Candidate preference created successfully",
                candidatePreferenceService.create(userId, body),
                request.getRequestURI()
        );

    }

    @PutMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<CandidatePreferenceResponse> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateCandidatePreferenceRequest body,
            HttpServletRequest request
    ) {

        return ApiResponse.success(
                "Candidate preference updated successfully",
                candidatePreferenceService.update(userId, body),
                request.getRequestURI()
        );

    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    public ApiResponse<Void> delete(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {

        candidatePreferenceService.delete(userId);

        return ApiResponse.success(
                "Candidate preference deleted successfully",
                request.getRequestURI()
        );

    }

}