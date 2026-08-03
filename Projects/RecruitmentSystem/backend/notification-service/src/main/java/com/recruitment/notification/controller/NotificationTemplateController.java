package com.recruitment.notification.controller;

import com.recruitment.notification.common.ApiResponse;
import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.CreateNotificationTemplateRequest;
import com.recruitment.notification.dto.request.UpdateNotificationTemplateRequest;
import com.recruitment.notification.dto.request.UpdateTemplateActiveRequest;
import com.recruitment.notification.dto.response.NotificationTemplateResponse;
import com.recruitment.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification-templates")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Notification Template", description = "Notification Template Management API")
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @PostMapping
    @Operation(summary = "Create notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(
            @Valid @RequestBody CreateNotificationTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification template created.", notificationTemplateService.create(request)));
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Update notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> update(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification template updated.",
                notificationTemplateService.update(templateId, request)));
    }

    @PatchMapping("/{templateId}/active")
    @Operation(summary = "Activate or deactivate notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> updateActive(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateTemplateActiveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification template status updated.",
                notificationTemplateService.updateActive(templateId, request.getActive())));
    }

    @GetMapping
    @Operation(summary = "Get notification templates")
    public ResponseEntity<ApiResponse<PageResponse<NotificationTemplateResponse>>> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1.")
            @Max(value = 100, message = "Size must not exceed 100.") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(notificationTemplateService.getAll(active, pageable)));
    }

}
