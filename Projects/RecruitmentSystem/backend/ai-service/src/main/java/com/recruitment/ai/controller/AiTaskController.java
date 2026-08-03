package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.common.PageResponse;
import com.recruitment.ai.dto.response.AiTaskResponse;
import com.recruitment.ai.service.AiTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/tasks")
@RequiredArgsConstructor
@Tag(name = "AI Task", description = "Read-only AI task infrastructure APIs")
public class AiTaskController {

    private final AiTaskService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get AI tasks visible to the current user")
    public ApiResponse<PageResponse<AiTaskResponse>> getTasks(@ParameterObject Pageable pageable) {
        return ApiResponse.success(service.getTasks(pageable));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get AI task by ID")
    public ApiResponse<AiTaskResponse> getById(
            @Parameter(description = "AI task ID")
            @PathVariable UUID taskId
    ) {
        return ApiResponse.success(service.getById(taskId));
    }

}
