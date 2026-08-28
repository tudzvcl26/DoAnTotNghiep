package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.dto.request.CareerChatRequest;
import com.recruitment.ai.dto.response.CareerChatResponse;
import com.recruitment.ai.service.CareerCompanionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/career")
@RequiredArgsConstructor
public class CareerCompanionController {

    private final CareerCompanionService careerCompanionService;

    @PostMapping("/chat")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<CareerChatResponse> chat(@Valid @RequestBody CareerChatRequest request) {
        return ApiResponse.success("Trợ lý nghề nghiệp đã tạo câu trả lời.", careerCompanionService.chat(request));
    }
}
