package com.recruitment.ai.explanation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ExplanationJsonValidator {
    private static final List<String> LIST_FIELDS = List.of(
            "strengths", "weaknesses", "highScoreReasons", "lowScoreReasons", "missingTechnologies",
            "careerSuggestions", "resumeImprovementChecklist", "skillRecommendations", "projectRecommendations",
            "certificationSuggestions", "keywordImprovements", "experienceImprovements", "educationImprovements",
            "learningRoadmap", "recommendedTechnologies", "recommendedCertifications", "portfolioImprovements");
    private static final Set<String> PRIORITIES = Set.of("HIGH", "MEDIUM", "LOW");
    private final ObjectMapper objectMapper;

    public JsonNode validate(String output) {
        try {
            JsonNode root = objectMapper.readTree(output);
            if (root == null || !root.isObject() || !text(root, "overallEvaluation")
                    || root.has("overallScore") || root.has("score")) throw invalid();
            for (String field : LIST_FIELDS) {
                JsonNode values = root.get(field);
                if (values == null || !values.isArray()) throw invalid();
                for (JsonNode value : values) if (!value.isTextual() || value.asText().isBlank()) throw invalid();
            }
            JsonNode gaps = root.get("gapExplanations");
            if (gaps == null || !gaps.isArray()) throw invalid();
            for (JsonNode gap : gaps) {
                if (!gap.isObject() || !text(gap, "area") || !text(gap, "gap") || !text(gap, "explanation")
                        || !PRIORITIES.contains(gap.path("priority").asText().toUpperCase())) throw invalid();
            }
            return root;
        } catch (BusinessException exception) { throw exception; }
        catch (Exception exception) { throw invalid(); }
    }

    private boolean text(JsonNode node, String field) {
        return node.has(field) && node.get(field).isTextual() && !node.get(field).asText().isBlank();
    }
    private BusinessException invalid() { return new BusinessException(ErrorCode.EXPLANATION_INVALID); }
}
