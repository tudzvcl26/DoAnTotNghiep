package com.recruitment.ai.entity;

import com.recruitment.ai.entity.enums.AiTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "ai_tasks",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ai_tasks_request_idempotency",
                columnNames = {"requested_by", "idempotency_key"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class AiTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiTaskStatus status = AiTaskStatus.PENDING;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "subject_type", length = 50)
    private String subjectType;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private Integer progress = 0;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "input_checksum", length = 128)
    private String inputChecksum;

    // Private, credential-free snapshot for durable background generation.
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "input_payload", columnDefinition = "JSONB")
    private String inputPayload;

    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "model_name", length = 150)
    private String modelName;

    @Column(name = "prompt_version", length = 100)
    private String promptVersion;

    @Column(name = "result_reference", length = 1024)
    private String resultReference;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Boolean retryable = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(name = "entity_version", nullable = false)
    private Long entityVersion;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
