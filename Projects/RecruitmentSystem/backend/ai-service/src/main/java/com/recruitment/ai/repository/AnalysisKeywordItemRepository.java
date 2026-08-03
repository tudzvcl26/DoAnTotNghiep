package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AnalysisKeywordItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisKeywordItemRepository extends JpaRepository<AnalysisKeywordItem, UUID> {

    List<AnalysisKeywordItem> findByAnalysisResultIdOrderByOrdinalPositionAsc(UUID analysisResultId);

    void deleteByAnalysisResultId(UUID analysisResultId);
}
