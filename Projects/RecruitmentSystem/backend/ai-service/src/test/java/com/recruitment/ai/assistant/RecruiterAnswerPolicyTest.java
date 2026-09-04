package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecruiterAnswerPolicyTest {
    private final RecruiterAnswerPolicy policy = new RecruiterAnswerPolicy(new ObjectMapper());

    @Test void summarizeJobStaysOnThePublishedJobAndRecruiterAudience() {
        assertThat(policy.accepts("""
                {"summary":"Vị trí Java Developer.","recommendations":["Trách nhiệm: phát triển API."],
                 "risks":[],"nextSteps":["Yêu cầu: Java và 3 năm kinh nghiệm."]}
                """, "SUMMARIZE_JOB")).isTrue();
        assertThat(policy.accepts("""
                {"summary":"Ứng viên phù hợp với CV.","recommendations":["Bạn nên học Java."],"risks":[],"nextSteps":[]}
                """, "SUMMARIZE_JOB")).isFalse();
    }
}
