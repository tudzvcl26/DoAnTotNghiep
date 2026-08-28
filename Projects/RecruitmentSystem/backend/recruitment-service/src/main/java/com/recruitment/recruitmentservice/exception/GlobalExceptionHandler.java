package com.recruitment.recruitmentservice.exception;

import com.recruitment.recruitmentservice.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ErrorCode.FORBIDDEN.getCode(),
                        ex.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .code(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {

        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.VALIDATION_ERROR.getCode())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Validation failed.")
                        .errors(errors)
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.BAD_REQUEST.getCode())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            Exception ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.BAD_REQUEST.getCode())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message("Malformed or invalid request.")
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("unhandled_exception type={} method={} path={}", ex.getClass().getSimpleName(),
                request.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        request.getRequestURI()
                ));

    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(
                        ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus()
                )
                .body(ApiResponse.error(
                        ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(),
                        ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage(),
                        request.getRequestURI()
                ));

    }

}
