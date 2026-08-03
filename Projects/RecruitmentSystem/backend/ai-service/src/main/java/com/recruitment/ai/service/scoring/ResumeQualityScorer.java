package com.recruitment.ai.service.scoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.ai.dto.response.ScoreDimensionResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ResumeQualityScorer {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[+()0-9 .-]{7,25}$");

    public ResumeQualityScore score(JsonNode facts, String extractedText) {
        Map<String, ScoreDimensionResponse> dimensions = new LinkedHashMap<>();
        dimensions.put("resumeCompleteness", completeness(facts));
        dimensions.put("educationQuality", countDimension(facts, "education", 10, 3, "education entries"));
        dimensions.put("experience", experience(facts));
        dimensions.put("technicalSkills", countDimension(facts, "technicalSkills", 15, 2, "technical skills"));
        dimensions.put("softSkills", countDimension(facts, "softSkills", 8, 2, "soft skills"));
        dimensions.put("projectRichness", countDimension(facts, "projects", 10, 3, "projects"));
        dimensions.put("formatting", formatting(extractedText));
        dimensions.put("keywordRichness", countDimension(facts, "keywords", 8, 1, "keywords"));
        dimensions.put("consistency", consistency(facts));
        int total = dimensions.values().stream().mapToInt(ScoreDimensionResponse::score).sum();
        return new ResumeQualityScore(Math.max(0, Math.min(100, total)), Map.copyOf(dimensions));
    }

    private ScoreDimensionResponse completeness(JsonNode facts) {
        String[] scalarFields = {"fullName", "email", "phone", "location", "summary"};
        String[] arrayFields = {"education", "experience", "projects", "skills", "technicalSkills"};
        int present = 0;
        for (String field : scalarFields) {
            present += hasText(facts.path(field)) ? 1 : 0;
        }
        for (String field : arrayFields) {
            present += facts.path(field).isArray() && !facts.path(field).isEmpty() ? 1 : 0;
        }
        int score = Math.round(present * 15f / 10f);
        return dimension(score, 15, present + " of 10 core sections contain facts");
    }

    private ScoreDimensionResponse experience(JsonNode facts) {
        int entries = arraySize(facts, "experience");
        int score = Math.min(20, entries * 7 + (hasText(facts.path("summary")) ? 2 : 0));
        return dimension(score, 20, entries + " experience entries");
    }

    private ScoreDimensionResponse formatting(String text) {
        int score = 0;
        int length = text == null ? 0 : text.length();
        long lines = text == null ? 0 : text.lines().filter(line -> !line.isBlank()).count();
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        long sectionCount = Set.of("education", "experience", "skills", "projects", "summary")
                .stream().filter(lower::contains).count();
        score += length >= 500 ? 3 : length >= 200 ? 2 : length > 0 ? 1 : 0;
        score += lines >= 10 ? 2 : lines >= 5 ? 1 : 0;
        score += sectionCount >= 3 ? 2 : sectionCount >= 1 ? 1 : 0;
        return dimension(score, 7, lines + " non-empty lines and " + sectionCount + " recognizable sections");
    }

    private ScoreDimensionResponse consistency(JsonNode facts) {
        int score = 0;
        String email = facts.path("email").asText("").trim();
        String phone = facts.path("phone").asText("").trim();
        score += EMAIL.matcher(email).matches() ? 2 : 0;
        score += PHONE.matcher(phone).matches() ? 2 : 0;
        score += hasText(facts.path("fullName")) ? 1 : 0;
        score += uniqueArray(facts.path("technicalSkills")) ? 1 : 0;
        score += uniqueArray(facts.path("keywords")) ? 1 : 0;
        return dimension(score, 7, "contact formats and duplicate-list checks");
    }

    private ScoreDimensionResponse countDimension(
            JsonNode facts,
            String field,
            int maximum,
            int pointsPerItem,
            String label
    ) {
        int count = arraySize(facts, field);
        return dimension(Math.min(maximum, count * pointsPerItem), maximum, count + " " + label);
    }

    private int arraySize(JsonNode facts, String field) {
        JsonNode value = facts.path(field);
        return value.isArray() ? value.size() : 0;
    }

    private boolean uniqueArray(JsonNode value) {
        if (!value.isArray()) {
            return false;
        }
        Set<String> unique = StreamSupport.stream(value.spliterator(), false)
                .map(this::itemText)
                .filter(text -> !text.isBlank())
                .map(text -> text.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return !unique.isEmpty() && unique.size() == value.size();
    }

    private String itemText(JsonNode value) {
        if (value.isTextual()) {
            return value.asText().trim();
        }
        return value.path("name").asText(value.toString()).trim();
    }

    private boolean hasText(JsonNode value) {
        return value.isTextual() && !value.asText().isBlank();
    }

    private ScoreDimensionResponse dimension(int score, int maximum, String rationale) {
        return new ScoreDimensionResponse(Math.min(score, maximum), maximum, rationale);
    }
}
