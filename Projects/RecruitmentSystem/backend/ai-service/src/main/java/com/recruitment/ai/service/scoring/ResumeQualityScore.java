package com.recruitment.ai.service.scoring;

import com.recruitment.ai.dto.response.ScoreDimensionResponse;

import java.util.Map;

public record ResumeQualityScore(
        int total,
        Map<String, ScoreDimensionResponse> dimensions
) {
}
