package com.recruitment.notification.entity;

import com.recruitment.notification.entity.enums.NotificationChannel;
import com.recruitment.notification.entity.enums.NotificationEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    @Column(name = "content_template", nullable = false, columnDefinition = "TEXT")
    private String contentTemplate;

    @Column(nullable = false)
    private boolean active;

}
