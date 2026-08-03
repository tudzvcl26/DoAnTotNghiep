package com.recruitment.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "resume_analysis_results")
@EntityListeners(AuditingEntityListener.class)
public class ResumeAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "resume_document_id", nullable = false, unique = true)
    private ResumeDocument resumeDocument;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ai_task_id", nullable = false)
    private AiTask aiTask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prompt_template_version_id", nullable = false)
    private PromptTemplateVersion promptTemplateVersion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_deployment_id", nullable = false)
    private ModelDeployment modelDeployment;

    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 100)
    private String promptVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_data", nullable = false, columnDefinition = "JSONB")
    private String structuredData;

    @Column(name = "quality_score", nullable = false)
    private Integer qualityScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown", nullable = false, columnDefinition = "JSONB")
    private String scoreBreakdown;

    @Column(name = "input_tokens", nullable = false)
    private Long inputTokens = 0L;

    @Column(name = "output_tokens", nullable = false)
    private Long outputTokens = 0L;

    @Column(name = "analysis_duration_ms", nullable = false)
    private Long analysisDurationMs;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisSkillItem> skillItems = new ArrayList<>();

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisKeywordItem> keywordItems = new ArrayList<>();

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
