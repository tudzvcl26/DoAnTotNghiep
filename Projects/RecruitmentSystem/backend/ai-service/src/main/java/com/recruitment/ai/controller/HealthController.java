package com.recruitment.ai.controller;

import com.recruitment.ai.common.ApiResponse;
import com.recruitment.ai.provider.ModelRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Health", description = "AI Service health API")
public class HealthController {

    private final ModelRouter modelRouter;
    private final HealthEndpoint healthEndpoint;

    @GetMapping("/api/v1/health")
    @Operation(summary = "Get AI Service health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        HealthComponent systemHealth = healthEndpoint.health();
        data.put("service", "ai-service");
        data.put("status", systemHealth.getStatus().getCode());
        data.put("phase", "COMPLETE");
        data.put("aiProviderAvailable", modelRouter.structuredGenerationProvider().descriptor().available());
        return ApiResponse.success(data);
    }

}
