package com.recruitment.ai.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "job_match_results")
@EntityListeners(AuditingEntityListener.class)
public class JobMatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_analysis_result_id", nullable = false)
    private ResumeAnalysisResult resumeAnalysisResult;

    @Column(name = "resume_document_id", nullable = false)
    private UUID resumeDocumentId;

    @Column(name = "resume_owner_user_id", nullable = false)
    private UUID resumeOwnerUserId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "job_company_id", nullable = false)
    private UUID jobCompanyId;

    @Column(name = "job_owner_user_id", nullable = false)
    private UUID jobOwnerUserId;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_skills", nullable = false, columnDefinition = "JSONB")
    private String matchedSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills", nullable = false, columnDefinition = "JSONB")
    private String missingSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_keywords", nullable = false, columnDefinition = "JSONB")
    private String matchedKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_keywords", nullable = false, columnDefinition = "JSONB")
    private String missingKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths", nullable = false, columnDefinition = "JSONB")
    private String strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weaknesses", nullable = false, columnDefinition = "JSONB")
    private String weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations", nullable = false, columnDefinition = "JSONB")
    private String recommendations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gap_analysis", nullable = false, columnDefinition = "JSONB")
    private String gapAnalysis;

    @Column(name = "matched_experience", nullable = false, length = 500)
    private String matchedExperience;

    @Column(name = "matched_education", nullable = false, length = 500)
    private String matchedEducation;

    @Column(name = "rule_version", nullable = false, length = 100)
    private String ruleVersion;

    @Column(name = "weights_version", nullable = false, length = 100)
    private String weightsVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weights_snapshot", nullable = false, columnDefinition = "JSONB")
    private String weightsSnapshot;

    @Column(name = "matching_duration_ms", nullable = false)
    private Long matchingDurationMs;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @OneToMany(mappedBy = "matchResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchScoreBreakdown> breakdowns = new ArrayList<>();

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
