package com.recruitment.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // =========================================================
    // COMMON
    // =========================================================

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON_500",
            "Internal server error"
    ),

    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "COMMON_400",
            "Bad request"
    ),

    VALIDATION_ERROR(
            HttpStatus.BAD_REQUEST,
            "COMMON_001",
            "Validation failed"
    ),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON_404",
            "Resource not found"
    ),

    // =========================================================
    // AUTH
    // =========================================================

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401",
            "Unauthorized"
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "AUTH_403",
            "Access denied"
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_001",
            "Invalid email or password"
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTH_002",
            "Email already exists"
    ),

    ROLE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH_003",
            "Role not found"
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH_004",
            "User not found"
    ),

    USER_DISABLED(
            HttpStatus.FORBIDDEN,
            "AUTH_005",
            "User account is disabled"
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_006",
            "Invalid token"
    ),

    TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_007",
            "Token has expired"
    ),

    REFRESH_TOKEN_NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "AUTH_008",
            "Refresh token not found"
    ),

    REFRESH_TOKEN_REVOKED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_009",
            "Refresh token has been revoked"
    ),

    REFRESH_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_010",
            "Refresh token has expired"
    ),

    INVALID_ROLE(
            HttpStatus.BAD_REQUEST,
            "AUTH_011",
            "Role is not available for management"
    ),

    ADMIN_SELF_LOCKOUT(
            HttpStatus.CONFLICT,
            "AUTH_012",
            "An administrator cannot remove their own admin access or disable their own account"
    ),

    ACTION_TOKEN_INVALID(
            HttpStatus.BAD_REQUEST,
            "AUTH_013",
            "The account action token is invalid or has already been used"
    ),

    ACTION_TOKEN_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH_014",
            "The account action token has expired"
    );

    private final HttpStatus status;

    private final String code;

    private final String message;

}
