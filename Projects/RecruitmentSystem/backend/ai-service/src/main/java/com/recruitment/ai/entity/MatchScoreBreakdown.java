package com.recruitment.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "match_score_breakdowns")
public class MatchScoreBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_result_id", nullable = false)
    private JobMatchResult matchResult;

    @Column(name = "dimension_code", nullable = false, length = 50)
    private String dimensionCode;

    @Column(name = "maximum_score", nullable = false)
    private Integer maximumScore;

    @Column(name = "actual_score", nullable = false)
    private Integer actualScore;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "ordinal_position", nullable = false)
    private Integer ordinalPosition;
}
