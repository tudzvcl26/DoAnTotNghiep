package com.recruitment.user.controller;

import com.recruitment.user.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("status", "UP");
        data.put("service", "user-service");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User Service is running",
                        data,
                        "/api/v1/health"
                )
        );
    }

}
