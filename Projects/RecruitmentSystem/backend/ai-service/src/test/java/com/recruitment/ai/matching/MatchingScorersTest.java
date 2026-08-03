package com.recruitment.ai.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.matching.model.JobRequirements;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.scorer.CertificateScorer;
import com.recruitment.ai.matching.scorer.EducationScorer;
import com.recruitment.ai.matching.scorer.ExperienceScorer;
import com.recruitment.ai.matching.scorer.KeywordScorer;
import com.recruitment.ai.matching.scorer.LanguageScorer;
import com.recruitment.ai.matching.scorer.ProjectScorer;
import com.recruitment.ai.matching.scorer.SkillScorer;
import com.recruitment.ai.matching.scorer.SoftSkillScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScorersTest {

    private MatchingContext context;

    @BeforeEach
    void setUp() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"technicalSkills":["Java","Docker"],"skills":[],"softSkills":["Teamwork"],
                 "experience":[{"duration":"3 years"}],"education":[{"degree":"Bachelor"}],
                 "projects":[{"description":"Java service"}],"certificates":[],
                 "languages":["English"],"keywords":["Java","Docker"]}
                """);
        JobRequirements requirements = new JobRequirements(
                List.of("Java", "Spring Boot"), List.of("Docker"), List.of("Teamwork"),
                List.of("English"), List.of("java", "spring", "docker"), 5, true, true);
        JobSnapshot job = new JobSnapshot(UUID.randomUUID(), "Java Engineer", "", "", "",
                "SENIOR", "PUBLISHED", true, UUID.randomUUID(), UUID.randomUUID());
        context = new MatchingContext(facts, job, requirements);
    }

    @Test void scoresTechnicalSkills() {
        assertThat(new SkillScorer().score(context, 40).actualScore()).isEqualTo(24);
    }

    @Test void scoresExperienceAgainstThreshold() {
        assertThat(new ExperienceScorer().score(context, 20).actualScore()).isEqualTo(12);
    }

    @Test void scoresEducationRequirement() {
        assertThat(new EducationScorer().score(context, 10).actualScore()).isEqualTo(10);
    }

    @Test void scoresProjectTechnologyCoverage() {
        assertThat(new ProjectScorer().score(context, 10).actualScore()).isEqualTo(3);
    }

    @Test void scoresMissingRequiredCertificate() {
        assertThat(new CertificateScorer().score(context, 5).actualScore()).isZero();
    }

    @Test void scoresLanguages() {
        assertThat(new LanguageScorer().score(context, 5).actualScore()).isEqualTo(5);
    }

    @Test void scoresSoftSkills() {
        assertThat(new SoftSkillScorer().score(context, 5).actualScore()).isEqualTo(5);
    }

    @Test void scoresKeywords() {
        assertThat(new KeywordScorer().score(context, 5).actualScore()).isEqualTo(3);
    }
}
