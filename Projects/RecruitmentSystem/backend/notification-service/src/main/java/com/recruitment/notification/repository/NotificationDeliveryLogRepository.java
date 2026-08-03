package com.recruitment.notification.repository;

import com.recruitment.notification.entity.NotificationDeliveryLog;
import com.recruitment.notification.entity.enums.NotificationDeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, UUID> {

    Page<NotificationDeliveryLog> findByStatus(NotificationDeliveryStatus status, Pageable pageable);

}
