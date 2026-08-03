package com.recruitment.ai.matching.model;

import java.util.List;

public record MatchingComputation(
        int overallScore,
        List<ScoreResult> breakdown,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        List<String> gapAnalysis,
        String matchedExperience,
        String matchedEducation
) {
}
