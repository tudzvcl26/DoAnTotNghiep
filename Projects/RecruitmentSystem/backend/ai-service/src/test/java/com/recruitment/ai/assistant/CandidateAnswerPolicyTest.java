package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CandidateAnswerPolicyTest {
    private final CandidateAnswerPolicy policy = new CandidateAnswerPolicy(new ObjectMapper());

    @Test void rejectsObservedHiringClaimsAndRecruiterDirections() {
        for (String sentence : new String[]{
                "Ứng viên Alex Morgan đã được tuyển dụng cho vị trí Java.",
                "Tổ chức phỏng vấn để yêu cầu ứng viên cung cấp dự án.",
                "Tạo một bài kiểm tra cho ứng viên để minh chứng khả năng.",
                "Không có yêu cầu cụ thể về Spring Boot trong công việc hiện tại.",
                "JD không yêu cầu Java nên bạn không cần ưu tiên kỹ năng này.",
                "Java và Spring Boot, hai ngôn ngữ quan trọng."}) {
            assertThat(policy.accepts("{\"summary\":\"" + sentence + "\"}", "SKILL_ROADMAP")).isFalse();
        }
    }

    @Test void requiresCareerMilestonesButAllowsConditionalCandidatePractice() {
        assertThat(policy.accepts("{\"summary\":\"Bạn nên bổ sung CV.\"}", "CAREER_ROADMAP")).isFalse();
        String grounded = "{\"summary\":\"Bạn có thể luyện Java.\",\"nextSteps\":[\"0–3 tháng — Mục tiêu: API; Hành động: luyện Java; Đầu ra: API có unit test.\",\"3–6 tháng — Mục tiêu: triển khai; Hành động: hoàn thiện demo; Đầu ra: demo chạy được.\"]}";
        assertThat(policy.accepts(grounded, "CAREER_ROADMAP")).isTrue();
        assertThat(policy.accepts("{\"summary\":\"Khoảng trống chưa có minh chứng Java.\",\"nextSteps\":[\"Hành động: làm bài thực hành; Đầu ra: API chạy được.\"]}", "SKILL_ROADMAP")).isTrue();
    }

    @Test void rejectsInternalQaTokensAndEmbeddedRawJson() {
        assertThat(policy.accepts("{\"summary\":\"QA Candidate trên localhost.\"}", "RESUME_IMPROVEMENT")).isFalse();
        assertThat(policy.accepts("{\"summary\":\"CV của bạn: {\\\"skill\\\":\\\"Java\\\"}\",\"recommendations\":[\"Sửa CV\"]}", "RESUME_IMPROVEMENT")).isFalse();
    }
}
