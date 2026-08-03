package com.recruitment.notification.repository;

import com.recruitment.notification.entity.NotificationUserState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationUserStateRepository extends JpaRepository<NotificationUserState, UUID> {

    Optional<NotificationUserState> findByNotificationIdAndUserId(UUID notificationId, UUID userId);

    List<NotificationUserState> findByUserIdAndNotificationIdIn(UUID userId, Collection<UUID> notificationIds);

}
