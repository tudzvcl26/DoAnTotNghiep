package com.recruitment.notification.controller;

import com.recruitment.notification.common.ApiResponse;
import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.BroadcastNotificationRequest;
import com.recruitment.notification.dto.request.CreateNotificationRequest;
import com.recruitment.notification.dto.response.NotificationResponse;
import com.recruitment.notification.dto.response.UnreadNotificationCountResponse;
import com.recruitment.notification.entity.enums.NotificationEventType;
import com.recruitment.notification.service.NotificationService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Notification Management API")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a personal notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created.", notificationService.create(request)));
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Broadcast a system announcement")
    public ResponseEntity<ApiResponse<NotificationResponse>> broadcast(
            @Valid @RequestBody BroadcastNotificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("System announcement created.", notificationService.broadcast(request)));
    }

    @GetMapping
    @Operation(summary = "Get notifications for the current user or an ADMIN-selected recipient")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) UUID recipientUserId,
            @RequestParam(required = false) NotificationEventType eventType,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1.")
            @Max(value = 100, message = "Size must not exceed 100.") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(recipientUserId, eventType, read, query, pageable)));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification detail")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(notificationId)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read.", notificationService.markAsRead(notificationId)));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read.", null));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Soft delete a notification for the current user")
    public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
        notificationService.delete(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

}
