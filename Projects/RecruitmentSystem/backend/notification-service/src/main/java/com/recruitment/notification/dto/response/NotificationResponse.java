package com.recruitment.notification.dto.response;

import com.recruitment.notification.entity.enums.NotificationEventType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private UUID id;
    private NotificationEventType eventType;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private String relatedResourceType;
    private UUID relatedResourceId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

}
