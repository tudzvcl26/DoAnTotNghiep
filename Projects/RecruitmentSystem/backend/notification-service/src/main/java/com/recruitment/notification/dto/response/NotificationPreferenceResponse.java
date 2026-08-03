package com.recruitment.notification.dto.response;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPreferenceResponse {

    private NotificationEventType eventType;
    private NotificationChannel channel;
    private boolean enabled;

}
