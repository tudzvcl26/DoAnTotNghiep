package com.recruitment.ai.entity;

import com.recruitment.ai.entity.enums.SkillCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "analysis_skill_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_analysis_skill",
                columnNames = {"analysis_result_id", "skill_name", "skill_category"}
        )
)
public class AnalysisSkillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private ResumeAnalysisResult analysisResult;

    @Column(name = "skill_name", nullable = false, length = 255)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_category", nullable = false, length = 30)
    private SkillCategory skillCategory;

    @Column(name = "ordinal_position", nullable = false)
    private Integer ordinalPosition;
}
