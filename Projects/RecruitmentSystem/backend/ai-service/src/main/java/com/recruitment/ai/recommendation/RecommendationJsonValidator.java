package com.recruitment.ai.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RecommendationJsonValidator {
    private static final Set<String> FORBIDDEN = Set.of("score", "overallScore", "approved", "rejected", "decision");
    private final ObjectMapper objectMapper;

    public JsonNode validateJob(String value) {
        return validate(value, Set.of("recommendationSummary", "gapSummary", "recommendationReason"));
    }

    public JsonNode validateCandidate(String value) {
        return validate(value, Set.of("recommendationSummary", "interviewRecommendation", "recommendationReason"));
    }

    private JsonNode validate(String value, Set<String> required) {
        try {
            JsonNode root = objectMapper.readTree(value);
            if (!root.isObject() || hasForbidden(root)) throw invalid();
            for (String field : required) {
                if (!root.hasNonNull(field) || !root.path(field).isTextual() || root.path(field).asText().isBlank()) {
                    throw invalid();
                }
            }
            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    private boolean hasForbidden(JsonNode node) {
        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                if (FORBIDDEN.contains(field.getKey()) || hasForbidden(field.getValue())) return true;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) if (hasForbidden(child)) return true;
        }
        return false;
    }

    private BusinessException invalid() {
        return new BusinessException(ErrorCode.RECOMMENDATION_INVALID);
    }
}
