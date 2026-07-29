package com.recruitment.recruitmentservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /*
     * ==========================
     * Common Errors
     * ==========================
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

    INVALID_PAGINATION_OR_SORT(
            HttpStatus.BAD_REQUEST,
            "COMMON_002",
            "Invalid pagination or sorting parameter."
    ),

    DATA_INTEGRITY_VIOLATION(
            HttpStatus.CONFLICT,
            "COMMON_003",
            "The request conflicts with existing data."
    ),

    /*
     * ==========================
     * Job Category
     * ==========================
     */

    JOB_CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "JC_001",
            "Job category not found."
    ),

    JOB_CATEGORY_NAME_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "JC_002",
            "Job category name already exists."
    ),

    JOB_CATEGORY_SLUG_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "JC_003",
            "Job category slug already exists."
    ),

    JOB_CATEGORY_HAS_CHILDREN(
            HttpStatus.BAD_REQUEST,
            "JC_004",
            "Cannot delete category because it has child categories."
    ),

    INVALID_PARENT_CATEGORY(
            HttpStatus.BAD_REQUEST,
            "JC_005",
            "Invalid parent category."
    ),

    JOB_CATEGORY_IN_USE(
            HttpStatus.CONFLICT,
            "JC_006",
            "Cannot delete category because it is used by jobs."
    ),

    /*
     * ==========================
     * Skill
     * ==========================
     */

    SKILL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SK_001",
            "Skill not found."
    ),

    SKILL_NAME_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "SK_002",
            "Skill name already exists."
    ),

    SKILL_SLUG_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "SK_003",
            "Skill slug already exists."
    ),

    /*
     * ==========================
     * Benefit
     * ==========================
     */

    BENEFIT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "BF_001",
            "Benefit not found."
    ),

    BENEFIT_NAME_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "BF_002",
            "Benefit name already exists."
    ),

    BENEFIT_SLUG_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "BF_003",
            "Benefit slug already exists."
    ),

    /*
     * ==========================
     * Job
     * ==========================
     */

    JOB_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "JOB_001",
            "Job not found."
    ),

    JOB_CODE_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "JOB_002",
            "Job code already exists."
    ),

    JOB_ALREADY_PUBLISHED(
            HttpStatus.BAD_REQUEST,
            "JOB_003",
            "Job has already been published."
    ),

    JOB_ALREADY_CLOSED(
            HttpStatus.BAD_REQUEST,
            "JOB_004",
            "Job has already been closed."
    ),

    INVALID_JOB_STATUS(
            HttpStatus.BAD_REQUEST,
            "JOB_005",
            "Invalid job status."
    );

    private final HttpStatus status;

    private final String code;

    private final String message;

}