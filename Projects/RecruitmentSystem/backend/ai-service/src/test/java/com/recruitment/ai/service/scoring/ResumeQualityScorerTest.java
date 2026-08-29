package com.recruitment.ai.service.scoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeQualityScorerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResumeQualityScorer scorer = new ResumeQualityScorer();

    @Test
    void calculatesDeterministicBoundedScoreAcrossNineDimensions() throws Exception {
        JsonNode facts = objectMapper.readTree("""
                {
                  "fullName":"Nguyen Van A","email":"a@example.com","phone":"+84901234567",
                  "location":"Ho Chi Minh City","summary":"Backend engineer",
                  "education":[{"school":"University"}],
                  "experience":[{"company":"Acme"},{"company":"Beta"}],
                  "projects":[{"name":"RecruitmentSystem"}],
                  "skills":["Communication"],
                  "technicalSkills":["Java","Spring Boot","PostgreSQL"],
                  "softSkills":["Teamwork","Communication"],
                  "keywords":["Java","Microservices","Spring"]
                }
                """);

        ResumeQualityScore first = scorer.score(facts,
                "Summary\nEducation\nExperience\nSkills\nProjects\n" + "Java backend experience. ".repeat(30));
        ResumeQualityScore second = scorer.score(facts,
                "Summary\nEducation\nExperience\nSkills\nProjects\n" + "Java backend experience. ".repeat(30));

        assertThat(first).isEqualTo(second);
        assertThat(first.total()).isBetween(0, 100);
        assertThat(first.dimensions()).hasSize(9);
        assertThat(first.dimensions().values().stream().mapToInt(d -> d.score()).sum())
                .isEqualTo(first.total());
        assertThat(first.dimensions().get("resumeCompleteness").rationale())
                .contains("mục thông tin cốt lõi").doesNotContain("core sections");
        assertThat(first.dimensions().get("technicalSkills").rationale())
                .contains("kỹ năng chuyên môn").doesNotContain("technical skills");
    }
}
