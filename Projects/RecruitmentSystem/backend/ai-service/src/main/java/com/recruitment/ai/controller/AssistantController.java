package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.dto.request.CandidateAssistantRequest;
import com.recruitment.ai.dto.request.RecruiterAssistantRequest;
import com.recruitment.ai.dto.response.AssistantResponseDto;
import com.recruitment.ai.service.AssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/assistant")
@RequiredArgsConstructor
@Tag(name = "Structured Assistants", description = "Task-based candidate and recruiter assistance from structured project data")
public class AssistantController {
    private final AssistantService assistantService;

    @PostMapping("/candidate")
    @PreAuthorize("hasAnyRole('CANDIDATE','ADMIN')")
    @Operation(summary = "Run a constrained candidate assistant task")
    public ApiResponse<AssistantResponseDto> candidate(@Valid @RequestBody CandidateAssistantRequest request) {
        return ApiResponse.success("Đã tạo nội dung hỗ trợ ứng viên.", assistantService.assistCandidate(request));
    }

    @PostMapping("/recruiter")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    @Operation(summary = "Run a constrained recruiter assistant task")
    public ApiResponse<AssistantResponseDto> recruiter(@Valid @RequestBody RecruiterAssistantRequest request) {
        return ApiResponse.success("Đã tạo nội dung hỗ trợ nhà tuyển dụng.", assistantService.assistRecruiter(request));
    }
}
