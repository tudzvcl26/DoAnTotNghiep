package com.recruitment.ai.dto.response;

public record MatchScoreBreakdownResponse(
        String dimension,
        int maximumScore,
        int actualScore,
        String reason
) {
}
