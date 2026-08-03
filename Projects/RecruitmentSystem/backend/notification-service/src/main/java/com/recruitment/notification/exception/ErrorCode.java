package com.recruitment.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Internal server error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "Bad request"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "Resource not found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "Access denied"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "COMMON_003", "The request conflicts with existing data."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "Notification not found."),
    NOTIFICATION_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_002", "Notification template not found."),
    NOTIFICATION_TEMPLATE_CODE_EXISTS(HttpStatus.CONFLICT, "NOTIFICATION_003", "Notification template code already exists."),
    INVALID_NOTIFICATION_PREFERENCE(HttpStatus.BAD_REQUEST, "NOTIFICATION_004", "System announcements must remain enabled for the IN_APP channel.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
