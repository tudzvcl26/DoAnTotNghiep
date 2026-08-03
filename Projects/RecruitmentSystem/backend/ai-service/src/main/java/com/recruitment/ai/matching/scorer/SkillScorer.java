package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SkillScorer implements MatchingScorer {
    @Override public String dimension() { return "technicalSkills"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        Set<String> resume = MatchingText.normalized(resumeSkills(context));
        List<String> required = context.requirements().requiredSkills();
        List<String> preferred = context.requirements().preferredSkills();
        if (required.isEmpty() && preferred.isEmpty()) {
            return new ScoreResult(dimension(), maximumScore, maximumScore,
                    "The job does not declare recognized technical skill requirements.");
        }
        long requiredMatched = required.stream().map(MatchingText::normalize).filter(resume::contains).count();
        long preferredMatched = preferred.stream().map(MatchingText::normalize).filter(resume::contains).count();
        double numerator = requiredMatched + preferredMatched * 0.5;
        double denominator = required.size() + preferred.size() * 0.5;
        int score = ScoringSupport.proportional(maximumScore, numerator, denominator);
        String reason = "Required skills %d/%d; preferred skills %d/%d."
                .formatted(requiredMatched, required.size(), preferredMatched, preferred.size());
        return new ScoreResult(dimension(), maximumScore, score, reason);
    }

    private List<String> resumeSkills(MatchingContext context) {
        List<String> result = new ArrayList<>(MatchingText.fieldValues(context.resumeFacts(), "technicalSkills"));
        result.addAll(MatchingText.fieldValues(context.resumeFacts(), "skills"));
        return result;
    }
}
