package com.recruitment.ai.entity;

import com.recruitment.ai.entity.enums.ModelCapability;
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
        name = "model_deployments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_model_deployment_identity",
                columnNames = {"provider_name", "model_name", "deployment_name", "capability"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class ModelDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(name = "deployment_name", nullable = false, length = 150)
    private String deploymentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ModelCapability capability;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "default_for_capability", nullable = false)
    private Boolean defaultForCapability = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private String configuration;

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
