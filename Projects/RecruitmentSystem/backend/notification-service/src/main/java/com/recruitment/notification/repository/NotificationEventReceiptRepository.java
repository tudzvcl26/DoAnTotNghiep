package com.recruitment.notification.repository;

import com.recruitment.notification.entity.NotificationEventReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationEventReceiptRepository extends JpaRepository<NotificationEventReceipt, UUID> {

    boolean existsByEventId(UUID eventId);

}
