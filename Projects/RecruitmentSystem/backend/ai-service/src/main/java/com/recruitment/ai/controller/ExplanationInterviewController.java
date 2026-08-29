package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.dto.response.InterviewPreparationResponse;
import com.recruitment.ai.dto.response.MatchExplanationResponse;
import com.recruitment.ai.service.ExplanationInterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/matching/{matchId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER','ADMIN')")
@Tag(name = "AI Explanation and Interview", description = "Grounded explanation and interview preparation for deterministic matches")
public class ExplanationInterviewController {
    private final ExplanationInterviewService service;

    @PostMapping("/explanation")
    @Operation(summary = "Generate an explanation and improvement plan without changing the deterministic score")
    public ApiResponse<MatchExplanationResponse> generateExplanation(@PathVariable UUID matchId) {
        return ApiResponse.success("Đã tạo phần giải thích độ phù hợp.", service.generateExplanation(matchId));
    }

    @GetMapping("/explanation")
    @Operation(summary = "Get the persisted matching explanation")
    public ApiResponse<MatchExplanationResponse> getExplanation(@PathVariable UUID matchId) {
        return ApiResponse.success(service.getExplanation(matchId));
    }

    @PostMapping("/interview")
    @Operation(summary = "Generate grounded technical, behavioral, HR, and project interview preparation")
    public ApiResponse<InterviewPreparationResponse> generateInterview(@PathVariable UUID matchId) {
        return ApiResponse.success("Đã tạo nội dung chuẩn bị phỏng vấn.", service.generateInterview(matchId));
    }

    @GetMapping("/interview")
    @Operation(summary = "Get the persisted interview preparation")
    public ApiResponse<InterviewPreparationResponse> getInterview(@PathVariable UUID matchId) {
        return ApiResponse.success(service.getInterview(matchId));
    }
}
