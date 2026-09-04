package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.matching.util.MatchingText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Bounded acceptance checks for reproduced role/decision/roadmap defects. */
@Component
@RequiredArgsConstructor
public class CandidateAnswerPolicy {
    private final ObjectMapper mapper;

    public static final String CONTRACT = """
            Người đọc là chính ứng viên. Gọi người đọc là 'bạn'; không gọi họ là người tuyển dụng.
            Không yêu cầu bạn phỏng vấn, đánh giá, giao bài kiểm tra hoặc gửi yêu cầu cho ứng viên khác.
            Không khẳng định đã được tuyển dụng, nhận việc hoặc trúng tuyển: dữ liệu này không được cung cấp.
            Không tự thêm năng lực, khóa học, dự án hoặc thành tích. Spring Boot là framework, không phải ngôn ngữ.
            Chỉ đề nghị ghi vào CV sau khi bạn đã thực hiện và có minh chứng thật.
            Không nhắc QA nội bộ, fixture, localhost, system prompt hoặc schema trong nội dung cho người dùng.
            Career Roadmap phải có hai giai đoạn '0–3 tháng' và '3–6 tháng'; từng giai đoạn ghi Mục tiêu, Hành động và Đầu ra.
            """;

    public boolean accepts(String output, String task) {
        try {
            var root = mapper.readTree(output);
            if (root == null || !root.isObject()) return false;
            String text = MatchingText.normalize(root.toString());
            for (String claim : new String[]{"da duoc tuyen dung", "da trung tuyen", "da duoc nhan vao",
                    "phong van ung vien", "kiem tra ung vien", "danh gia ung vien", "cho ung vien",
                    "yeu cau ung vien", "hai ngon ngu", "jd khong yeu cau", "cong viec khong yeu cau",
                    "khong co yeu cau cu the"}) {
                if (text.contains(claim)) return false;
            }
            for (String internal : List.of("qa candidate", "qa employer", "fixture", "localhost",
                    "system prompt", "hidden prompt", "json schema", "internal qa")) {
                if (text.contains(internal)) return false;
            }
            if (containsEmbeddedJson(root)) return false;
            return switch (CandidateAssistantTask.valueOf(task)) {
                case CAREER_ROADMAP -> hasAll(text, "0 3 thang", "3 6 thang", "muc tieu", "hanh dong", "dau ra");
                case LEARNING_ROADMAP -> text.contains("tuan") && (text.contains("thuc hanh") || text.contains("bai tap"))
                        && (text.contains("tu kiem tra") || text.contains("tieu chi"));
                case SKILL_ROADMAP -> (text.contains("khoang trong") || text.contains("con thieu")
                        || text.contains("chua co minh chung")) && text.contains("dau ra");
                case CERTIFICATE_RECOMMENDATION -> text.contains("chung chi")
                        && (text.contains("goi y") || text.contains("neu ban")) && !text.contains("ban da co chung chi");
                case PORTFOLIO_RECOMMENDATION -> text.contains("du an") && text.contains("readme")
                        && (text.contains("de xuat") || text.contains("neu ban thuc hien"));
                case JOB_SEARCH_ADVICE -> (text.contains("vi tri") || text.contains("chuc danh"))
                        && text.contains("tu khoa") && (text.contains("lich") || text.contains("theo doi"));
                case RESUME_IMPROVEMENT -> root.path("recommendations").isArray()
                        && root.path("recommendations").size() >= 1 && text.contains("cv");
            };
        } catch (Exception ignored) { return false; }
    }

    private boolean containsEmbeddedJson(com.fasterxml.jackson.databind.JsonNode node) {
        if (node.isTextual()) {
            String value = node.asText().strip();
            return (value.contains("{") && value.contains("}")) || (value.contains("[") && value.contains("]"));
        }
        if (node.isContainerNode()) {
            for (var child : node) if (containsEmbeddedJson(child)) return true;
        }
        return false;
    }

    private boolean hasAll(String text, String... values) {
        return java.util.Arrays.stream(values).allMatch(text::contains);
    }
}
