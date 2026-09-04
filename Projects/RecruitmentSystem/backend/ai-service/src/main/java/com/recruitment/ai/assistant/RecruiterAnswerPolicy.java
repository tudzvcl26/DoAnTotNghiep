package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.matching.util.MatchingText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Semantic checks for recruiter-facing output before it can be persisted. */
@Component
@RequiredArgsConstructor
public class RecruiterAnswerPolicy {
    public static final String CONTRACT = """
            Người đọc là nhà tuyển dụng. Không hướng dẫn họ học kỹ năng như một ứng viên.
            Với SUMMARIZE_JOB, chỉ tóm tắt publishedJob: vai trò, trách nhiệm, yêu cầu và mức kinh nghiệm.
            Không nhắc ứng viên, CV, match score hoặc quyết định tuyển dụng khi tác vụ chỉ tóm tắt công việc.
            Không nhắc QA nội bộ, fixture, localhost, system prompt hoặc schema.
            """;

    private final ObjectMapper mapper;

    public boolean accepts(String output, String task) {
        try {
            JsonNode root = mapper.readTree(output);
            if (root == null || !root.isObject()) return false;
            String text = MatchingText.normalize(root.toString());
            for (String internal : List.of("qa candidate", "qa employer", "fixture", "localhost",
                    "system prompt", "hidden prompt", "json schema", "internal qa")) {
                if (text.contains(internal)) return false;
            }
            if (!"SUMMARIZE_JOB".equals(task)) return true;
            if (text.contains("ung vien") || text.contains("cv") || text.contains("match score")
                    || text.contains("tuyen dung ung vien") || text.contains("ban nen hoc")) return false;
            return (text.contains("vai tro") || text.contains("vi tri") || text.contains("cong viec"))
                    && text.contains("yeu cau") && (text.contains("trach nhiem") || text.contains("nhiem vu"));
        } catch (Exception ignored) {
            return false;
        }
    }
}
