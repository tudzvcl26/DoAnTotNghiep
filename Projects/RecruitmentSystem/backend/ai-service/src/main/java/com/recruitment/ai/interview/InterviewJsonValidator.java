package com.recruitment.ai.interview;

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
public class InterviewJsonValidator {
    private static final List<String> CATEGORIES = List.of(
            "technicalQuestions", "behavioralQuestions", "hrQuestions", "projectQuestions");
    private static final Set<String> DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    private final ObjectMapper objectMapper;

    public JsonNode validate(String output) {
        try {
            JsonNode root = objectMapper.readTree(output);
            if (root == null || !root.isObject() || root.has("score") || root.has("overallScore")) throw invalid();
            for (String category : CATEGORIES) {
                JsonNode questions = root.get(category);
                if (questions == null || !questions.isArray() || questions.isEmpty()) throw invalid();
                for (JsonNode question : questions) {
                    if (!question.isObject() || !text(question, "question") || !text(question, "expectedAnswerOutline")
                            || !text(question, "whyInterviewerAsks") || !text(question, "relatedResumeSection")
                            || !DIFFICULTIES.contains(question.path("difficulty").asText().toUpperCase())) throw invalid();
                }
            }
            return root;
        } catch (BusinessException exception) { throw exception; }
        catch (Exception exception) { throw invalid(); }
    }

    private boolean text(JsonNode node, String field) {
        return node.has(field) && node.get(field).isTextual() && !node.get(field).asText().isBlank();
    }
    private BusinessException invalid() { return new BusinessException(ErrorCode.INTERVIEW_INVALID); }
}
