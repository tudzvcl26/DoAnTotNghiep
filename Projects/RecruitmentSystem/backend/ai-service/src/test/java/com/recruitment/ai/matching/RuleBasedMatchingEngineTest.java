package com.recruitment.ai.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.matching.engine.RuleBasedMatchingEngine;
import com.recruitment.ai.matching.model.JobRequirements;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.matching.model.MatchingComputation;
import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.scorer.CertificateScorer;
import com.recruitment.ai.matching.scorer.EducationScorer;
import com.recruitment.ai.matching.scorer.ExperienceScorer;
import com.recruitment.ai.matching.scorer.KeywordScorer;
import com.recruitment.ai.matching.scorer.LanguageScorer;
import com.recruitment.ai.matching.scorer.ProjectScorer;
import com.recruitment.ai.matching.scorer.SkillScorer;
import com.recruitment.ai.matching.scorer.SoftSkillScorer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedMatchingEngineTest {

    @Test
    void orchestratesEveryWeightedScorerAndBuildsDeterministicGaps() throws Exception {
        MatchingProperties properties = new MatchingProperties();
        List<MatchingScorer> scorers = List.of(new SkillScorer(), new ExperienceScorer(), new EducationScorer(),
                new ProjectScorer(), new CertificateScorer(), new LanguageScorer(), new SoftSkillScorer(),
                new KeywordScorer());
        RuleBasedMatchingEngine engine = new RuleBasedMatchingEngine(properties, scorers);
        JsonNode facts = new ObjectMapper().readTree("""
                {"technicalSkills":["Java"],"skills":[],"softSkills":[],"experience":[],
                 "education":[],"projects":[],"certificates":[],"languages":[],"keywords":["Java"]}
                """);
        JobRequirements requirements = new JobRequirements(List.of("Java", "Spring Boot"), List.of(),
                List.of("Communication"), List.of("English"), List.of("java", "spring"), 3, true, false);
        JobSnapshot job = new JobSnapshot(UUID.randomUUID(), "Java Engineer", "", "", "", "MIDDLE",
                "PUBLISHED", true, UUID.randomUUID(), UUID.randomUUID());

        MatchingComputation result = engine.match(new MatchingContext(facts, job, requirements));

        assertThat(result.breakdown()).hasSize(8);
        assertThat(result.overallScore()).isEqualTo(result.breakdown().stream().mapToInt(item -> item.actualScore()).sum());
        assertThat(result.matchedSkills()).containsExactly("Java");
        assertThat(result.missingSkills()).containsExactly("Spring Boot");
        assertThat(result.gapAnalysis()).anyMatch(item -> item.contains("Spring Boot"));
        assertThat(result.recommendations()).isNotEmpty();
    }
}
