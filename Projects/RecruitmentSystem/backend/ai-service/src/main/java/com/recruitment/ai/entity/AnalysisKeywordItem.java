package com.recruitment.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "analysis_keyword_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_analysis_keyword",
                columnNames = {"analysis_result_id", "keyword"}
        )
)
public class AnalysisKeywordItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private ResumeAnalysisResult analysisResult;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "ordinal_position", nullable = false)
    private Integer ordinalPosition;
}
