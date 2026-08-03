package com.recruitment.notification.controller;

import com.recruitment.notification.common.ApiResponse;
import com.recruitment.notification.dto.request.UpdateNotificationPreferencesRequest;
import com.recruitment.notification.dto.response.NotificationPreferenceResponse;
import com.recruitment.notification.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/preferences")
@Tag(name = "Notification Preference", description = "Notification Preference Management API")
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @GetMapping
    @Operation(summary = "Get current user notification preferences")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getMyPreferences() {
        return ResponseEntity.ok(ApiResponse.success(notificationPreferenceService.getMyPreferences()));
    }

    @PutMapping
    @Operation(summary = "Update current user notification preferences")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> updateMyPreferences(
            @Valid @RequestBody UpdateNotificationPreferencesRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated.",
                notificationPreferenceService.updateMyPreferences(request)));
    }

}
