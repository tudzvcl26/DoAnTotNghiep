package com.recruitment.notification.repository;

import com.recruitment.notification.entity.NotificationPreference;
import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByUserIdOrderByEventTypeAscChannelAsc(UUID userId);

    Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(
            UUID userId,
            NotificationEventType eventType,
            NotificationChannel channel
    );

}
