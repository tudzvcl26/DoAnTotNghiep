package com.recruitment.auth.controller;

import com.recruitment.auth.common.ApiResponse;
import com.recruitment.auth.config.DevActionTokenProperties;
import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.exception.BusinessException;
import com.recruitment.auth.exception.ErrorCode;
import com.recruitment.auth.service.DevAccountActionTokenDelivery;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/dev/action-token")
public class DevActionTokenController {
    private final DevAccountActionTokenDelivery delivery;
    private final DevActionTokenProperties properties;

    @GetMapping
    public ApiResponse<Map<String, String>> latest(@RequestParam String email,
                                                   @RequestParam AccountActionPurpose purpose,
                                                   @RequestHeader(value = "X-Dev-Token-Key", required = false) String accessKey,
                                                   HttpServletRequest request) {
        String configured = properties.getAccessKey();
        if (configured == null || configured.length() < 16 || accessKey == null || !MessageDigest.isEqual(
                configured.getBytes(StandardCharsets.UTF_8), accessKey.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        String token = delivery.latest(email, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTION_TOKEN_INVALID));
        return ApiResponse.success("Development action token retrieved", Map.of("token", token), request.getRequestURI());
    }
}
