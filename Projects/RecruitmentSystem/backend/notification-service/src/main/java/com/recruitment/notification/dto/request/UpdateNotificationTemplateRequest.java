package com.recruitment.notification.dto.request;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationTemplateRequest {

    @NotNull(message = "Event type is required.")
    private NotificationEventType eventType;

    @NotNull(message = "Channel is required.")
    private NotificationChannel channel;

    @NotBlank(message = "Title template is required.")
    @Size(max = 200, message = "Title template must not exceed 200 characters.")
    private String titleTemplate;

    @NotBlank(message = "Content template is required.")
    private String contentTemplate;

}
