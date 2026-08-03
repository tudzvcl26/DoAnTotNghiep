package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AnalysisSkillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisSkillItemRepository extends JpaRepository<AnalysisSkillItem, UUID> {

    List<AnalysisSkillItem> findByAnalysisResultIdOrderByOrdinalPositionAsc(UUID analysisResultId);

    void deleteByAnalysisResultId(UUID analysisResultId);
}
