package com.recruitment.notification.service;

import com.recruitment.notification.dto.response.NotificationResponse;

import java.util.UUID;

public interface PushNotifier {

    void notify(UUID recipientUserId, NotificationResponse notification);

}
