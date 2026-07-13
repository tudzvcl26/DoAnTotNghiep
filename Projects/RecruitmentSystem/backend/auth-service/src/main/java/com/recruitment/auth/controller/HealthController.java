package com.recruitment.auth.controller;

import com.recruitment.auth.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("status", "UP");
        data.put("service", "auth-service");
        data.put("version", "1.0.0");

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Authentication Service is running",
                        data,
                        "/api/v1/health"
                )
        );
    }

}