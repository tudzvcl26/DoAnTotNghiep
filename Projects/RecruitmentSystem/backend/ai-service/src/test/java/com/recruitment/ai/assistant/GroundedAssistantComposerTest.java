package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroundedAssistantComposerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GroundedAssistantComposer composer = new GroundedAssistantComposer(mapper);
    private final CandidateAnswerPolicy candidatePolicy = new CandidateAnswerPolicy(mapper);
    private final RecruiterAnswerPolicy recruiterPolicy = new RecruiterAnswerPolicy(mapper);
    private static final String CONTEXT = """
            {"resumeFacts":{"fullName":"Tên trong CV","technicalSkills":["Java"],
             "projects":["Thư viện Java"]},"deterministicMatchContext":{
             "authoritativeMatch":{"matchedSkills":["Java"],"missingSkills":["Spring Boot","PostgreSQL"]},
             "publishedJob":{"title":"Java Backend Developer","requirements":"Java, Spring Boot, PostgreSQL",
             "responsibilities":"Phát triển API","experienceLevel":"JUNIOR"}}}
            """;

    @Test void everyCandidateTaskProducesTaskSpecificAcceptedGroundedJson() throws Exception {
        for (CandidateAssistantTask task : CandidateAssistantTask.values()) {
            String output = composer.candidate(task.name(), CONTEXT);
            assertThat(candidatePolicy.accepts(output, task.name())).as(task.name()).isTrue();
            assertThat(output).containsAnyOf("Java", "Spring Boot", "PostgreSQL")
                    .doesNotContain("QA", "localhost", "đã được tuyển dụng");
            assertThat(mapper.readTree(output).path("recommendations").isArray()).isTrue();
        }
    }

    @Test void recruiterSummaryUsesOnlyPublishedJobAndPassesAudiencePolicy() {
        String context = """
                {"publishedJob":{"title":"Java Backend Developer","responsibilities":"Phát triển API",
                 "requirements":"Java và Spring Boot","experienceLevel":"JUNIOR"},
                 "resumeFacts":{"fullName":"Không được dùng"}}
                """;
        String output = composer.recruiterJobSummary(context);
        assertThat(recruiterPolicy.accepts(output, "SUMMARIZE_JOB")).isTrue();
        assertThat(output).contains("Java Backend Developer", "Phát triển API", "Java và Spring Boot")
                .doesNotContain("Không được dùng", "CV của bạn", "Ứng viên");
    }
}
