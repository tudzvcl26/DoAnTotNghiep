package com.recruitment.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadNotificationCountResponse {

    private long unreadCount;

}
