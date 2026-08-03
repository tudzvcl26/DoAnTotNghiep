package com.recruitment.notification.dto.request;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferenceItemRequest {

    @NotNull(message = "Event type is required.")
    private NotificationEventType eventType;

    @NotNull(message = "Channel is required.")
    private NotificationChannel channel;

    @NotNull(message = "Enabled value is required.")
    private Boolean enabled;

}
