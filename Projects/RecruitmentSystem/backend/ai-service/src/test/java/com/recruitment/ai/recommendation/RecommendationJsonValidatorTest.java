package com.recruitment.ai.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecommendationJsonValidatorTest {
    private final RecommendationJsonValidator validator = new RecommendationJsonValidator(new ObjectMapper());

    @Test
    void acceptsGroundedNarrativeWithoutScore() {
        var result = validator.validateJob("""
                {"recommendationSummary":"Strong alignment","gapSummary":"One gap","recommendationReason":"Evidence-based"}
                """);
        assertThat(result.path("recommendationSummary").asText()).isEqualTo("Strong alignment");
    }

    @Test
    void rejectsAnyGeneratedScoreOrDecision() {
        assertThatThrownBy(() -> validator.validateCandidate("""
                {"recommendationSummary":"Aligned","interviewRecommendation":"Discuss Java",
                 "recommendationReason":"Evidence","overallScore":99}
                """)).isInstanceOf(BusinessException.class);
    }
}
