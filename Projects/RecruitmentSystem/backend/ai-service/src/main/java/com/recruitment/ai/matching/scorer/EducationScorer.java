package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

@Component
public class EducationScorer implements MatchingScorer {
    @Override public String dimension() { return "education"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        boolean present = !MatchingText.fieldValues(context.resumeFacts(), "education").isEmpty();
        boolean required = context.requirements().degreeRequired();
        int score = !required || present ? maximumScore : 0;
        String reason = required
                ? (present ? "A degree requirement is declared and education evidence is present."
                : "A degree requirement is declared but education evidence is absent.")
                : "The job does not declare a minimum degree requirement.";
        return new ScoreResult(dimension(), maximumScore, score, reason);
    }
}
