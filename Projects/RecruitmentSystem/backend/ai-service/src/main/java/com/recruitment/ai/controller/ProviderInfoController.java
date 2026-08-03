package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.service.ProviderInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/providers")
@RequiredArgsConstructor
@Tag(name = "AI Provider", description = "AI provider and model routing information")
public class ProviderInfoController {

    private final ProviderInfoService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get configured AI provider information")
    public ApiResponse<Map<String, Object>> getProviderInfo() {
        return ApiResponse.success(service.getProviderInfo());
    }

}
