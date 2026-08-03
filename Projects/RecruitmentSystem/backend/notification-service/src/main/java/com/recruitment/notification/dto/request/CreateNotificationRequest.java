package com.recruitment.notification.dto.request;

import com.recruitment.notification.entity.enums.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CreateNotificationRequest {

    @NotNull(message = "Recipient user id is required.")
    private UUID recipientUserId;

    @NotNull(message = "Event type is required.")
    private NotificationEventType eventType;

    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must not exceed 200 characters.")
    private String title;

    @NotBlank(message = "Content is required.")
    @Size(max = 4000, message = "Content must not exceed 4000 characters.")
    private String content;

    private Map<String, Object> payload;

    @Size(max = 50, message = "Related resource type must not exceed 50 characters.")
    private String relatedResourceType;

    private UUID relatedResourceId;

}
