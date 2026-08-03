package com.recruitment.notification.controller;

import com.recruitment.notification.common.ApiResponse;
import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.response.NotificationDeliveryLogResponse;
import com.recruitment.notification.entity.enums.NotificationDeliveryStatus;
import com.recruitment.notification.service.NotificationDeliveryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notification-delivery-logs")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Notification Delivery Log", description = "Notification Delivery Audit API")
public class NotificationDeliveryLogController {

    private final NotificationDeliveryLogService notificationDeliveryLogService;

    @GetMapping
    @Operation(summary = "Get notification delivery logs")
    public ResponseEntity<ApiResponse<PageResponse<NotificationDeliveryLogResponse>>> getAll(
            @RequestParam(required = false) NotificationDeliveryStatus status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be at least 1.")
            @Max(value = 100, message = "Size must not exceed 100.") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(notificationDeliveryLogService.getAll(status, pageable)));
    }

}
