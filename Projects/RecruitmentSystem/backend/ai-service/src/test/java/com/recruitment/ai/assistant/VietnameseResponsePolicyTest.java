package com.recruitment.ai.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VietnameseResponsePolicyTest {

    private final VietnameseResponsePolicy policy = new VietnameseResponsePolicy();

    @Test
    void acceptsVietnameseNaturalLanguageWithTechnicalTerms() {
        assertThat(policy.isVietnameseNaturalLanguage(
                "Bạn nên cải thiện Java, Spring Boot, REST API, PostgreSQL và JWT để phù hợp hơn với vị trí này."))
                .isTrue();
    }

    @Test
    void rejectsClearlyEnglishNaturalLanguage() {
        assertThat(policy.isVietnameseNaturalLanguage(
                "You should improve your skills and focus on career experience."))
                .isFalse();
        assertThat(policy.isVietnameseNaturalLanguage(
                "The candidate should focus on improving their Java and Spring Boot skills by working on more "
                        + "complex projects. Additionally, they should consider taking online courses and seek "
                        + "mentorship from a community of backend engineers."))
                .isFalse();
    }

    @Test
    void rejectsCjkNaturalLanguage() {
        assertThat(policy.isVietnameseNaturalLanguage("あなたの履歴書を改善してください")).isFalse();
    }
}
