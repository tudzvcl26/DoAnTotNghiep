package com.recruitment.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Entity
@Table(name = "assistant_sessions")
@EntityListeners(AuditingEntityListener.class)
public class AssistantSession {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "requested_by", nullable = false) private UUID requestedBy;
    @Column(name = "assistant_type", nullable = false, length = 30) private String assistantType;
    @Column(name = "task_type", nullable = false, length = 100) private String taskType;
    @Column(name = "job_id") private UUID jobId;
    @Column(name = "resume_document_id") private UUID resumeDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_result_id") private JobMatchResult matchResult;
    @Column(name = "correlation_id", nullable = false, length = 100) private String correlationId;
    @Version @Column(name = "entity_version", nullable = false) private Long entityVersion;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
