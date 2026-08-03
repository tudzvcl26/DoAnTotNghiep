package com.recruitment.ai.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.entity.JobMatchResult;
import com.recruitment.ai.entity.MatchScoreBreakdown;
import com.recruitment.ai.matching.model.JobSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerationContextBuilder {
    private final ObjectMapper objectMapper;

    public String build(JobMatchResult match, JobSnapshot job) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode authoritative = root.putObject("authoritativeMatch");
            authoritative.put("overallScore", match.getOverallScore());
            authoritative.put("ruleVersion", match.getRuleVersion());
            authoritative.put("weightsVersion", match.getWeightsVersion());
            authoritative.set("matchedSkills", objectMapper.readTree(match.getMatchedSkills()));
            authoritative.set("missingSkills", objectMapper.readTree(match.getMissingSkills()));
            authoritative.set("matchedKeywords", objectMapper.readTree(match.getMatchedKeywords()));
            authoritative.set("missingKeywords", objectMapper.readTree(match.getMissingKeywords()));
            authoritative.set("strengths", objectMapper.readTree(match.getStrengths()));
            authoritative.set("weaknesses", objectMapper.readTree(match.getWeaknesses()));
            authoritative.set("recommendations", objectMapper.readTree(match.getRecommendations()));
            authoritative.set("gapAnalysis", objectMapper.readTree(match.getGapAnalysis()));
            authoritative.put("matchedExperience", match.getMatchedExperience());
            authoritative.put("matchedEducation", match.getMatchedEducation());
            ArrayNode breakdown = authoritative.putArray("scoreBreakdown");
            match.getBreakdowns().stream().sorted(java.util.Comparator.comparing(MatchScoreBreakdown::getOrdinalPosition))
                    .forEach(item -> {
                        ObjectNode node = breakdown.addObject();
                        node.put("dimension", item.getDimensionCode());
                        node.put("maximumScore", item.getMaximumScore());
                        node.put("actualScore", item.getActualScore());
                        node.put("reason", item.getReason());
                    });

            root.set("resumeFacts", objectMapper.readTree(match.getResumeAnalysisResult().getStructuredData()));
            ObjectNode jobNode = root.putObject("publishedJob");
            jobNode.put("id", job.id().toString());
            jobNode.put("title", job.title());
            jobNode.put("description", job.description());
            jobNode.put("requirements", job.requirements());
            jobNode.put("responsibilities", job.responsibilities());
            jobNode.put("experienceLevel", job.experienceLevel());
            jobNode.put("status", job.status());
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not build AI generation context.", exception);
        }
    }
}
