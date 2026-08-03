package com.recruitment.ai.dto.response;

import com.recruitment.ai.entity.enums.SkillCategory;

public record AnalysisSkillItemResponse(
        String name,
        SkillCategory category
) {
}
