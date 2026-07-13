package com.recruitment.auth.controller;

import com.recruitment.auth.common.ApiResponse;
import com.recruitment.auth.dto.request.LoginRequest;
import com.recruitment.auth.dto.request.LogoutRequest;
import com.recruitment.auth.dto.request.RefreshTokenRequest;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.dto.response.UserProfileResponse;
import com.recruitment.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {

        AuthResponse response = authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Register successfully.",
                        response,
                        httpRequest.getRequestURI()
                ));
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        AuthResponse response = authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successfully.",
                        response,
                        httpRequest.getRequestURI()
                )
        );
    }

    @Operation(summary = "Refresh Access Token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {

        AuthResponse response =
                authenticationService.refreshToken(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Refresh token successfully.",
                        response,
                        httpRequest.getRequestURI()
                )
        );
    }

    @Operation(
            summary = "Logout",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest
    ) {

        authenticationService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successfully.",
                        httpRequest.getRequestURI()
                )
        );
    }

    @Operation(
            summary = "Current User",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            HttpServletRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user.",
                        authenticationService.getCurrentUser(),
                        request.getRequestURI()
                )
        );
    }

}