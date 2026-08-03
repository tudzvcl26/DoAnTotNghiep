package com.recruitment.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Entity
@Table(name = "job_recommendations")
@EntityListeners(AuditingEntityListener.class)
public class JobRecommendation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_result_id", nullable = false, unique = true) private JobMatchResult matchResult;
    @Column(name = "resume_document_id", nullable = false) private UUID resumeDocumentId;
    @Column(name = "candidate_user_id", nullable = false) private UUID candidateUserId;
    @Column(name = "job_id", nullable = false) private UUID jobId;
    @Column(name = "overall_score", nullable = false) private Integer overallScore;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_task_id", nullable = false) private AiTask aiTask;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prompt_template_version_id", nullable = false) private PromptTemplateVersion promptTemplateVersion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_deployment_id", nullable = false) private ModelDeployment modelDeployment;
    @Column(name = "provider_name", nullable = false, length = 100) private String providerName;
    @Column(name = "model_name", nullable = false, length = 150) private String modelName;
    @Column(name = "prompt_version", nullable = false, length = 100) private String promptVersion;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendation_data", nullable = false, columnDefinition = "JSONB") private String recommendationData;
    @Column(name = "input_tokens", nullable = false) private Long inputTokens = 0L;
    @Column(name = "output_tokens", nullable = false) private Long outputTokens = 0L;
    @Column(name = "generation_duration_ms", nullable = false) private Long generationDurationMs;
    @Column(name = "correlation_id", nullable = false, length = 100) private String correlationId;
    @Version @Column(name = "entity_version", nullable = false) private Long entityVersion;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
