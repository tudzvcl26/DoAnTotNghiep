package com.recruitment.application.outbox;

import com.recruitment.application.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "application_outbox_events")
public class ApplicationOutboxEvent extends BaseEntity {
    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;
    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;
    @Column(name = "routing_key", nullable = false, length = 120)
    private String routingKey;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;
    @Column(nullable = false)
    private Integer attempts;
    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
}
