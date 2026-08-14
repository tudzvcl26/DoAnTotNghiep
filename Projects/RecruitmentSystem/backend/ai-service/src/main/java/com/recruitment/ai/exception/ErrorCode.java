package com.recruitment.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_COMMON_500", "Internal server error.", true),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "AI_COMMON_400", "Bad request.", false),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "AI_COMMON_001", "Validation failed.", false),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AI_AUTH_401", "Unauthorized.", false),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AI_AUTH_403", "Access denied.", false),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "AI_COMMON_003", "The request conflicts with existing data.", false),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_TASK_001", "AI task not found.", false),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RESUME_001", "Resume document not found.", false),
    RESUME_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "AI_RESUME_002", "Resume file is required.", false),
    RESUME_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "AI_RESUME_003", "Resume file exceeds the 10 MB limit.", false),
    RESUME_FILE_TYPE_UNSUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "AI_RESUME_004", "Only PDF, DOCX, and TXT resume files are supported.", false),
    RESUME_EXTRACTION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "AI_RESUME_005", "Resume text extraction failed.", false),
    RESUME_TEXT_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "AI_RESUME_006", "Resume does not contain extractable text.", false),
    RESUME_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RESUME_007", "Resume analysis not found.", false),
    RESUME_ANALYSIS_INVALID(HttpStatus.BAD_GATEWAY, "AI_RESUME_008", "AI provider returned invalid resume analysis JSON.", true),
    RESUME_FILE_READ_FAILED(HttpStatus.BAD_REQUEST, "AI_RESUME_009", "Resume file could not be read.", false),
    RESUME_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RESUME_010", "Active resume analysis prompt is not configured.", true),
    RESUME_MODEL_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RESUME_011", "Resume analysis model deployment is not configured.", true),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_MATCH_001", "Matching result not found.", false),
    MATCH_RESUME_NOT_ANALYZED(HttpStatus.CONFLICT, "AI_MATCH_002", "Resume must be analyzed before matching.", false),
    MATCH_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_MATCH_003", "Job not found.", false),
    MATCH_JOB_NOT_PUBLISHED(HttpStatus.CONFLICT, "AI_MATCH_004", "Only an active published job can be matched.", false),
    MATCH_UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_MATCH_005", "A required business service is unavailable.", true),
    EXPLANATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_EXPLANATION_001", "Matching explanation not found.", false),
    EXPLANATION_INVALID(HttpStatus.BAD_GATEWAY, "AI_EXPLANATION_002", "AI provider returned invalid explanation JSON.", true),
    EXPLANATION_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_EXPLANATION_003", "Active matching explanation prompt is not configured.", true),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_INTERVIEW_001", "Interview preparation not found.", false),
    INTERVIEW_INVALID(HttpStatus.BAD_GATEWAY, "AI_INTERVIEW_002", "AI provider returned invalid interview preparation JSON.", true),
    INTERVIEW_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_INTERVIEW_003", "Active interview preparation prompt is not configured.", true),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RECOMMENDATION_001", "Recommendation not found.", false),
    RECOMMENDATION_INVALID(HttpStatus.BAD_GATEWAY, "AI_RECOMMENDATION_002", "AI provider returned invalid recommendation JSON.", true),
    RECOMMENDATION_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RECOMMENDATION_003", "Active recommendation prompt is not configured.", true),
    RECOMMENDATION_RESUME_REQUIRED(HttpStatus.BAD_REQUEST, "AI_RECOMMENDATION_004", "An analyzed resume is required.", false),
    RECOMMENDATION_CONSENT_REQUIRED(HttpStatus.CONFLICT, "AI_RECOMMENDATION_005", "Candidate recommendation consent is required.", false),
    ASSISTANT_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI_ASSISTANT_001", "AI provider returned invalid assistant JSON.", true),
    ASSISTANT_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_ASSISTANT_002", "Active assistant prompt is not configured.", true),
    ASSISTANT_CONTEXT_INVALID(HttpStatus.BAD_REQUEST, "AI_ASSISTANT_003", "The selected assistant task requires additional structured context.", false),
    PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_001", "AI provider is not configured or unavailable.", true),
    STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_STORAGE_001", "AI object storage is unavailable.", true);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final boolean retryable;

}
