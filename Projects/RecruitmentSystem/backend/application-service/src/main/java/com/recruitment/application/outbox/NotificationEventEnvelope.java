package com.recruitment.application.outbox;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class NotificationEventEnvelope {
    private UUID eventId;
    private int eventVersion;
    private String eventType;
    private String sourceService;
    private LocalDateTime occurredAt;
    private List<UUID> recipientUserIds;
    private String title;
    private String content;
    private String relatedResourceType;
    private UUID relatedResourceId;
    private Map<String, Object> payload;
}
