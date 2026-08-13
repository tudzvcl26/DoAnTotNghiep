package com.recruitment.application.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /*
     * Common Errors
     */
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

    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT,
            "COMMON_003",
            "The request conflicts with existing data."
    ),

    /*
     * Application Specific Errors
     */
    APPLICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "APP_001",
            "Application not found."
    ),

    DUPLICATE_APPLICATION(
            HttpStatus.CONFLICT,
            "APP_002",
            "Candidate has already applied for this job."
    ),

    JOB_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "APP_003",
            "Job not found."
    ),

    JOB_NOT_ACCEPTING_APPLICATIONS(
            HttpStatus.BAD_REQUEST,
            "APP_004",
            "Job is not accepting applications."
    ),

    APPLICATION_DEADLINE_PASSED(
            HttpStatus.BAD_REQUEST,
            "APP_005",
            "Application deadline has passed."
    ),

    CANDIDATE_PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "APP_006",
            "Candidate profile not found."
    ),

    INVALID_APPLICATION_STATUS_TRANSITION(
            HttpStatus.BAD_REQUEST,
            "APP_007",
            "Invalid application status transition."
    ),

    APPLICATION_ALREADY_TERMINAL(
            HttpStatus.BAD_REQUEST,
            "APP_008",
            "Application status is already in a terminal state."
    ),

    CANNOT_WITHDRAW_APPLICATION(
            HttpStatus.BAD_REQUEST,
            "APP_009",
            "Application cannot be withdrawn in its current status."
    ),

    CURRENT_RESUME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "APP_010",
            "An active current resume is required before applying."
    ),

    APPLICATION_RESUME_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "APP_011",
            "Application resume snapshot was not found."
    ),

    DOWNSTREAM_BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "DEP_400",
            "A downstream service rejected the request."
    ),

    DOWNSTREAM_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "DEP_401",
            "Downstream authentication failed."
    ),

    DOWNSTREAM_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "DEP_403",
            "Downstream access was denied."
    ),

    DOWNSTREAM_INVALID_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "DEP_502",
            "A downstream service returned an invalid response."
    ),

    DOWNSTREAM_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "DEP_503",
            "A required downstream service is unavailable."
    ),

    DOWNSTREAM_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "DEP_504",
            "A downstream service timed out."
    );

    private final HttpStatus status;

    private final String code;

    private final String message;

}
