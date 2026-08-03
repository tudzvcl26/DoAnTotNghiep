package com.recruitment.ai.dto.response;

public record ScoreDimensionResponse(
        int score,
        int maximum,
        String rationale
) {
}
