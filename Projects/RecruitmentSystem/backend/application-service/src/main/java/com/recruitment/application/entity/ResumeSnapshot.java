package com.recruitment.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "resume_snapshots")
public class ResumeSnapshot extends BaseEntity {

    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "snapshot_data", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String snapshotData;

    @Column(name = "resume_version", length = 20)
    private String resumeVersion;

}
