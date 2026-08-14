package com.recruitment.auth.controller;

import com.recruitment.auth.common.ApiResponse;
import com.recruitment.auth.dto.request.LoginRequest;
import com.recruitment.auth.dto.request.LogoutRequest;
import com.recruitment.auth.dto.request.RefreshTokenRequest;
import com.recruitment.auth.dto.request.RegisterRequest;
import com.recruitment.auth.dto.request.ForgotPasswordRequest;
import com.recruitment.auth.dto.request.ResetPasswordRequest;
import com.recruitment.auth.dto.request.ResendVerificationRequest;
import com.recruitment.auth.dto.request.VerifyEmailRequest;
import com.recruitment.auth.dto.response.AuthResponse;
import com.recruitment.auth.dto.response.UserProfileResponse;
import com.recruitment.auth.service.AuthenticationService;
import com.recruitment.auth.service.AccountActionService;
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
    private final AccountActionService accountActionService;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest body, HttpServletRequest request) {
        accountActionService.requestPasswordReset(body.getEmail());
        return ResponseEntity.accepted().body(ApiResponse.success(
                "If the account exists, password reset instructions have been prepared.", request.getRequestURI()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest body, HttpServletRequest request) {
        accountActionService.resetPassword(body.getToken(), body.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully.", request.getRequestURI()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest body, HttpServletRequest request) {
        accountActionService.verifyEmail(body.getToken());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully.", request.getRequestURI()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest body, HttpServletRequest request) {
        accountActionService.resendVerification(body.getEmail());
        return ResponseEntity.accepted().body(ApiResponse.success(
                "If the account is eligible, verification instructions have been prepared.", request.getRequestURI()));
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
