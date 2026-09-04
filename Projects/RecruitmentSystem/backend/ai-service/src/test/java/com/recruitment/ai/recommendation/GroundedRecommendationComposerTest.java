package com.recruitment.ai.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.entity.JobMatchResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroundedRecommendationComposerTest {
    @Test void keepsCvEvidenceAndJobGapsInTheirAuthoritativeRoles() {
        JobMatchResult match = new JobMatchResult();
        match.setOverallScore(42);
        match.setRuleVersion("rules-v2");
        match.setMatchedSkills("[\"Java\"]");
        match.setMissingSkills("[\"Spring Boot\"]");
        var composer = new GroundedRecommendationComposer(new ObjectMapper());

        var job = composer.job(match);
        assertThat(job.path("recommendationReason").asText()).contains("Java").doesNotContain("Spring Boot");
        assertThat(job.path("gapSummary").asText()).contains("Spring Boot").doesNotContain("Java");
        assertThat(job.toString()).doesNotContain("local", "route", "regression", "QA");

        var candidate = composer.candidate(match);
        assertThat(candidate.path("interviewRecommendation").asText()).contains("Spring Boot");
        assertThat(candidate.path("recommendationReason").asText()).contains("Java", "không phải quyết định tuyển dụng");
    }
}
