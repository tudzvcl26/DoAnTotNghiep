package com.recruitment.ai.explanation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplanationJsonValidatorTest {
    private final ExplanationJsonValidator validator = new ExplanationJsonValidator(new ObjectMapper());

    @Test
    void acceptsCompleteNarrativeAndRejectsProviderScore() {
        String json = """
                {"overallEvaluation":"Grounded explanation","strengths":["Java"],"weaknesses":[],
                 "highScoreReasons":["Skills"],"lowScoreReasons":[],"missingTechnologies":[],
                 "careerSuggestions":[],"resumeImprovementChecklist":[],"skillRecommendations":[],
                 "projectRecommendations":[],"certificationSuggestions":[],"keywordImprovements":[],
                 "experienceImprovements":[],"educationImprovements":[],
                 "gapExplanations":[{"area":"Skills","gap":"Kafka","priority":"HIGH","explanation":"Missing"}],
                 "learningRoadmap":[],"recommendedTechnologies":[],"recommendedCertifications":[],"portfolioImprovements":[]}
                """;
        assertThat(validator.validate(json).path("overallEvaluation").asText()).isEqualTo("Grounded explanation");
        assertThatThrownBy(() -> validator.validate(json.replaceFirst("\\{", "{\"overallScore\":99,")))
                .isInstanceOf(BusinessException.class);
    }
}
