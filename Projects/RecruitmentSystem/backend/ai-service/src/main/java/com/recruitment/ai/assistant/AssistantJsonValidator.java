package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AssistantJsonValidator {
    private static final Set<String> FORBIDDEN = Set.of(
            "score", "overallScore", "approved", "rejected", "decision", "businessState");
    private final ObjectMapper objectMapper;

    public JsonNode validate(String value) {
        try {
            JsonNode root = objectMapper.readTree(value);
            if (!root.isObject() || hasForbidden(root) || !text(root, "summary")
                    || !array(root, "recommendations") || !array(root, "risks") || !array(root, "nextSteps")) {
                throw invalid();
            }
            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid();
        }
    }

    private boolean text(JsonNode root, String field) {
        return root.hasNonNull(field) && root.path(field).isTextual() && !root.path(field).asText().isBlank();
    }

    private boolean array(JsonNode root, String field) {
        return root.has(field) && root.path(field).isArray();
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
        return new BusinessException(ErrorCode.ASSISTANT_RESPONSE_INVALID);
    }
}
