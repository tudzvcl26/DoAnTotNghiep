package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SoftSkillScorer implements MatchingScorer {
    @Override public String dimension() { return "softSkills"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        Set<String> resume = MatchingText.normalized(MatchingText.fieldValues(context.resumeFacts(), "softSkills"));
        if (context.requirements().softSkills().isEmpty()) {
            return new ScoreResult(dimension(), maximumScore, maximumScore, "Công việc chưa nêu yêu cầu kỹ năng mềm; ứng viên không bị trừ điểm ở tiêu chí này.");
        }
        long matched = context.requirements().softSkills().stream().map(MatchingText::normalize).filter(resume::contains).count();
        int score = ScoringSupport.proportional(maximumScore, matched, context.requirements().softSkills().size());
        return new ScoreResult(dimension(), maximumScore, score,
                ScoringSupport.countReason("Kỹ năng mềm", (int) matched, context.requirements().softSkills().size()));
    }
}
