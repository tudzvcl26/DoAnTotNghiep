package com.recruitment.recruitmentservice.entity;

import com.recruitment.recruitmentservice.entity.enums.EmploymentType;
import com.recruitment.recruitmentservice.entity.enums.ExperienceLevel;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 50)
    private String jobCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 10)
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.DRAFT;

    @Column(nullable = false)
    private Integer quantity = 1;

    private LocalDate applicationDeadline;

    private LocalDateTime publishedAt;

    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private Boolean remoteAllowed = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private JobCategory category;

}