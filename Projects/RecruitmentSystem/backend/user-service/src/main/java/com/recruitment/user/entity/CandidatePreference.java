package com.recruitment.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidate_preferences")
public class CandidatePreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @DecimalMin("0.0")
    @Column(name = "salary_minimum", precision = 19, scale = 2)
    private BigDecimal salaryMinimum;

    @DecimalMin("0.0")
    @Column(name = "salary_maximum", precision = 19, scale = 2)
    private BigDecimal salaryMaximum;

    @Size(min = 3, max = 3)
    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_period", length = 30)
    private SalaryPeriod salaryPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 30)
    private AvailabilityStatus availabilityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_arrangement", length = 30)
    private WorkArrangement workArrangement;

    @Builder.Default
    @Column(name = "recommendation_consent", nullable = false)
    private Boolean recommendationConsent = false;

}
