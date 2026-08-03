package com.recruitment.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "ai_match_explanations")
@EntityListeners(AuditingEntityListener.class)
public class AiMatchExplanation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_result_id", nullable = false, unique = true) private JobMatchResult matchResult;
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
    @Column(name = "explanation_data", nullable = false, columnDefinition = "JSONB") private String explanationData;
    @Column(name = "input_tokens", nullable = false) private Long inputTokens = 0L;
    @Column(name = "output_tokens", nullable = false) private Long outputTokens = 0L;
    @Column(name = "generation_duration_ms", nullable = false) private Long generationDurationMs;
    @Column(name = "correlation_id", nullable = false, length = 100) private String correlationId;
    @Version @Column(name = "entity_version", nullable = false) private Long entityVersion;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
