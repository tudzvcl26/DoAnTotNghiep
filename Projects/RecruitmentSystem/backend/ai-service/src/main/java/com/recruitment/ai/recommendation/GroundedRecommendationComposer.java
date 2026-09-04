package com.recruitment.ai.recommendation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.entity.JobMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds user-facing recommendations only from the versioned matching result.
 * It deliberately does not copy free-form CV or job prose, so facts cannot move
 * between the candidate and job roles during generation.
 */
@Component
@RequiredArgsConstructor
public class GroundedRecommendationComposer {
    public static final String POLICY_VERSION = "grounded-recommendation-v1";
    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() { };
    private final ObjectMapper objectMapper;

    public JsonNode job(JobMatchResult match) {
        List<String> matched = read(match.getMatchedSkills());
        List<String> missing = read(match.getMissingSkills());
        ObjectNode result = objectMapper.createObjectNode();
        result.put("recommendationSummary", score(match));
        result.put("gapSummary", missing.isEmpty()
                ? "CHƯA XÁC MINH: Kết quả đối chiếu không ghi nhận kỹ năng bắt buộc còn thiếu."
                : "CHƯA XÁC MINH: CV chưa có bằng chứng cho kỹ năng bắt buộc: " + String.join(", ", missing) + ".");
        result.put("recommendationReason", matched.isEmpty()
                ? "BẰNG CHỨNG: Chưa có kỹ năng bắt buộc nào được xác nhận từ CV là phù hợp với công việc."
                : "BẰNG CHỨNG: Kỹ năng trong CV khớp yêu cầu công việc: " + String.join(", ", matched) + ".");
        return result;
    }

    public JsonNode candidate(JobMatchResult match) {
        List<String> matched = read(match.getMatchedSkills());
        List<String> missing = read(match.getMissingSkills());
        ObjectNode result = objectMapper.createObjectNode();
        result.put("recommendationSummary", score(match));
        result.put("interviewRecommendation", missing.isEmpty()
                ? "KHUYẾN NGHỊ: Xác minh chiều sâu của các bằng chứng đã khớp trong buổi phỏng vấn."
                : "KHUYẾN NGHỊ: Ưu tiên xác minh các kỹ năng chưa thấy bằng chứng trong CV: " + String.join(", ", missing) + ".");
        result.put("recommendationReason", (matched.isEmpty()
                ? "BẰNG CHỨNG: Chưa xác nhận được kỹ năng bắt buộc nào từ CV."
                : "BẰNG CHỨNG: Kỹ năng trong CV khớp yêu cầu công việc: " + String.join(", ", matched) + ".")
                + " Kết quả này không phải quyết định tuyển dụng.");
        return result;
    }

    private String score(JobMatchResult match) {
        return "KẾT QUẢ QUY TẮC: " + match.getOverallScore() + "/100 theo " + match.getRuleVersion() + ".";
    }

    private List<String> read(String value) {
        try {
            return value == null || value.isBlank() ? List.of() : objectMapper.readValue(value, STRINGS);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid persisted matching evidence.", exception);
        }
    }
}
