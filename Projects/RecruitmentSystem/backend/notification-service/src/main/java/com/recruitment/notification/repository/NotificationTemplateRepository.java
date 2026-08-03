package com.recruitment.notification.repository;

import com.recruitment.notification.entity.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByCode(String code);

    Page<NotificationTemplate> findByActive(boolean active, Pageable pageable);

}
