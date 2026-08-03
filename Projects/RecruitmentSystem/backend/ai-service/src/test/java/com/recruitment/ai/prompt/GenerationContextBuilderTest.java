package com.recruitment.ai.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.entity.JobMatchResult;
import com.recruitment.ai.entity.MatchScoreBreakdown;
import com.recruitment.ai.entity.ResumeAnalysisResult;
import com.recruitment.ai.matching.model.JobSnapshot;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class GenerationContextBuilderTest {
    @Test
    void preservesAuthoritativeScoreAndGroundingInputs() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JobMatchResult match = new JobMatchResult();
        match.setOverallScore(73); match.setRuleVersion("rules-v1"); match.setWeightsVersion("weights-v1");
        match.setMatchedSkills("[\"Java\"]"); match.setMissingSkills("[\"Kafka\"]");
        match.setMatchedKeywords("[]"); match.setMissingKeywords("[]"); match.setStrengths("[]");
        match.setWeaknesses("[]"); match.setRecommendations("[]"); match.setGapAnalysis("[]");
        match.setMatchedExperience("Matched"); match.setMatchedEducation("Matched");
        ResumeAnalysisResult analysis = new ResumeAnalysisResult();
        analysis.setStructuredData("{\"projects\":[\"RecruitmentSystem\"]}"); match.setResumeAnalysisResult(analysis);
        MatchScoreBreakdown item = new MatchScoreBreakdown(); item.setDimensionCode("technicalSkills");
        item.setMaximumScore(40); item.setActualScore(30); item.setReason("Java matched"); item.setOrdinalPosition(0);
        match.getBreakdowns().add(item);
        JobSnapshot job = new JobSnapshot(UUID.randomUUID(), "Java Engineer", "Build services", "Java", "Develop",
                "MIDDLE", "PUBLISHED", true, UUID.randomUUID(), UUID.randomUUID());

        JsonNode context = mapper.readTree(new GenerationContextBuilder(mapper).build(match, job));
        assertThat(context.path("authoritativeMatch").path("overallScore").asInt()).isEqualTo(73);
        assertThat(context.path("authoritativeMatch").path("scoreBreakdown")).hasSize(1);
        assertThat(context.path("resumeFacts").path("projects").get(0).asText()).isEqualTo("RecruitmentSystem");
        assertThat(context.path("publishedJob").path("status").asText()).isEqualTo("PUBLISHED");
    }
}
