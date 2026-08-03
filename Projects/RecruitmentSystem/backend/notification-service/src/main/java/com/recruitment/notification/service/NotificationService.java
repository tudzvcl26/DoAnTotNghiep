package com.recruitment.notification.service;

import com.recruitment.notification.common.PageResponse;
import com.recruitment.notification.dto.request.BroadcastNotificationRequest;
import com.recruitment.notification.dto.request.CreateNotificationRequest;
import com.recruitment.notification.dto.response.NotificationResponse;
import com.recruitment.notification.dto.response.UnreadNotificationCountResponse;
import com.recruitment.notification.entity.enums.NotificationEventType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse create(CreateNotificationRequest request);

    NotificationResponse broadcast(BroadcastNotificationRequest request);

    PageResponse<NotificationResponse> getNotifications(
            UUID recipientUserId,
            NotificationEventType eventType,
            Boolean read,
            String query,
            Pageable pageable
    );

    NotificationResponse getById(UUID notificationId);

    NotificationResponse markAsRead(UUID notificationId);

    void markAllAsRead();

    void delete(UUID notificationId);

    UnreadNotificationCountResponse getUnreadCount();

}
