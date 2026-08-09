package com.recruitment.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "api-gateway",
                "version", "1.0.0");
    }
}
