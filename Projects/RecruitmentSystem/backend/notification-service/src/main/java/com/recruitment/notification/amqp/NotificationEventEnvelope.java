package com.recruitment.notification.amqp;

import com.recruitment.notification.entity.enums.NotificationEventType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class NotificationEventEnvelope {

    private UUID eventId;
    private NotificationEventType eventType;
    private String sourceService;
    private LocalDateTime occurredAt;
    private List<UUID> recipientUserIds;
    private String title;
    private String content;
    private String relatedResourceType;
    private UUID relatedResourceId;
    private Map<String, Object> payload;

}
