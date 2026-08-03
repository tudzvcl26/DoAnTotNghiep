package com.recruitment.ai.exception;

import com.recruitment.ai.dto.response.AiErrorResponse;
import com.recruitment.ai.util.CorrelationIds;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return response(exception.getErrorCode(), exception.getErrorCode().getMessage(), null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return response(ErrorCode.FORBIDDEN, exception.getMessage(), null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        return response(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage(), details, request);
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<AiErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return response(ErrorCode.BAD_REQUEST, exception.getMessage(), null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AiErrorResponse> handleDataIntegrityViolation(HttpServletRequest request) {
        return response(ErrorCode.DATA_INTEGRITY_VIOLATION,
                ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage(), null, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<AiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return response(ErrorCode.RESUME_FILE_TOO_LARGE,
                ErrorCode.RESUME_FILE_TOO_LARGE.getMessage(), null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled {} for {}", exception.getClass().getSimpleName(), request.getRequestURI(), exception);
        return response(ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), null, request);
    }

    private ResponseEntity<AiErrorResponse> response(
            ErrorCode errorCode,
            String message,
            Map<String, String> details,
            HttpServletRequest request
    ) {
        AiErrorResponse body = AiErrorResponse.builder()
                .success(false)
                .code(errorCode.getCode())
                .message(message)
                .retryable(errorCode.isRetryable())
                .correlationId(CorrelationIds.current(request))
                .path(request.getRequestURI())
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

}
