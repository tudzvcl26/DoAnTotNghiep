package com.recruitment.notification.dto.response;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationTemplateResponse {

    private UUID id;
    private String code;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String titleTemplate;
    private String contentTemplate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
