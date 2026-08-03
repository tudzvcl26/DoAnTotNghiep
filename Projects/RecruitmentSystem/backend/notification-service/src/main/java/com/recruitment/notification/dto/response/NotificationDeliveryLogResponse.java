package com.recruitment.notification.dto.response;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationDeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationDeliveryLogResponse {

    private UUID id;
    private UUID notificationId;
    private UUID userId;
    private NotificationChannel channel;
    private NotificationDeliveryStatus status;
    private int attemptNumber;
    private LocalDateTime attemptedAt;
    private LocalDateTime deliveredAt;
    private String errorMessage;
    private LocalDateTime createdAt;

}
