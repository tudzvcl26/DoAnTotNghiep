package com.recruitment.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

@Getter
@Setter
@Entity
@Table(
        name = "prompt_template_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_prompt_template_code_version",
                columnNames = {"template_code", "version_number"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class PromptTemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "system_prompt", nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "user_prompt_template", nullable = false, columnDefinition = "TEXT")
    private String userPromptTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_schema", columnDefinition = "JSONB")
    private String outputSchema;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(name = "created_by")
    private UUID createdBy;

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
