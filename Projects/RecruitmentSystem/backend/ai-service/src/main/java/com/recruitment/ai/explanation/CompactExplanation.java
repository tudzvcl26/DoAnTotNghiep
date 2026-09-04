package com.recruitment.ai.explanation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/** Builds explanation prose only from authoritative matching evidence. */
@Component
@RequiredArgsConstructor
public class CompactExplanation {
    public static final String POLICY_VERSION = "grounded-explanation-v1";
    private final ObjectMapper mapper;
    public String context(String original) {
        try {
            JsonNode source = mapper.readTree(original);
            ObjectNode root = mapper.createObjectNode();
            JsonNode match = source.path("authoritativeMatch");
            for (String key : List.of("overallScore", "matchedSkills", "missingSkills", "matchedExperience", "matchedEducation", "scoreBreakdown"))
                root.set(key, match.path(key));
            root.put("jobTitle", source.path("publishedJob").path("title").asText());
            // Full scoring evidence is retained; omit duplicated CV/JD and derived lists.
            return mapper.writeValueAsString(root);
        } catch (Exception error) { throw new BusinessException(ErrorCode.EXPLANATION_INVALID); }
    }

    public String compose(String generated, String original) {
        try {
            JsonNode source = mapper.readTree(original);
            JsonNode match = source.path("authoritativeMatch");
            if (!match.path("overallScore").canConvertToInt()) throw invalid();
            ObjectNode result = mapper.createObjectNode();
            for (String field : List.of("strengths", "weaknesses", "highScoreReasons", "lowScoreReasons", "missingTechnologies",
                    "careerSuggestions", "resumeImprovementChecklist", "skillRecommendations", "projectRecommendations", "certificationSuggestions",
                    "keywordImprovements", "experienceImprovements", "educationImprovements", "gapExplanations", "learningRoadmap",
                    "recommendedTechnologies", "recommendedCertifications", "portfolioImprovements")) result.putArray(field);
            String jobTitle = source.path("publishedJob").path("title").asText("công việc đã chọn");
            result.put("overallEvaluation", "KẾT QUẢ QUY TẮC: " + match.path("overallScore").asInt()
                    + "/100 cho " + jobTitle + ". Chỉ các bằng chứng trong kết quả đối chiếu được sử dụng.");
            for (String field : List.of("strengths", "weaknesses")) result.set(field, match.path(field));
            result.set("highScoreReasons", match.path("strengths"));
            result.set("lowScoreReasons", match.path("weaknesses"));
            result.set("missingTechnologies", match.path("missingSkills"));
            result.set("recommendedTechnologies", match.path("missingSkills"));
            JsonNode missing = match.path("missingSkills");
            if (missing.isArray() && !missing.isEmpty()) {
                String skills = join(missing);
                missing.forEach(value -> {
                    if (!value.isTextual() || value.asText().isBlank()) return;
                    ObjectNode gap = result.withArray("gapExplanations").addObject();
                    gap.put("area", "Kỹ năng chuyên môn");
                    gap.put("gap", value.asText());
                    gap.put("priority", "HIGH");
                    gap.put("explanation", "CV chưa có bằng chứng cho kỹ năng bắt buộc này.");
                });
                result.withArray("careerSuggestions").add("KHUYẾN NGHỊ: Cân nhắc học và thực hành " + skills
                        + " nếu phù hợp mục tiêu; CV hiện chưa có bằng chứng cho các kỹ năng này.");
                result.withArray("resumeImprovementChecklist").add("Chỉ bổ sung " + skills
                        + " vào CV sau khi có minh chứng học tập, dự án hoặc công việc thực tế.");
                result.withArray("learningRoadmap").add("Tạo bài thực hành hoặc dự án nhỏ dùng " + skills
                        + ", sau đó ghi rõ phạm vi và kết quả có thể xác minh.");
            } else {
                result.withArray("careerSuggestions").add("KHUYẾN NGHỊ: Duy trì các minh chứng hiện có và xác minh chiều sâu khi phỏng vấn.");
                result.withArray("resumeImprovementChecklist").add("Giữ mô tả CV trung thực, cụ thể và có thể xác minh.");
            }
            return mapper.writeValueAsString(result);
        } catch (BusinessException error) { throw error; }
        catch (Exception error) { throw invalid(); }
    }
    private String join(JsonNode values) {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        values.forEach(value -> { if (value.isTextual() && !value.asText().isBlank()) result.add(value.asText()); });
        return String.join(", ", result);
    }
    private BusinessException invalid() { return new BusinessException(ErrorCode.EXPLANATION_INVALID); }
}
