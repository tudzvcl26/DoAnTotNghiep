package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LanguageScorer implements MatchingScorer {
    @Override public String dimension() { return "languages"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        Set<String> resume = MatchingText.normalized(MatchingText.fieldValues(context.resumeFacts(), "languages"));
        if (context.requirements().languages().isEmpty()) {
            return new ScoreResult(dimension(), maximumScore, maximumScore, "No language requirement is declared; this dimension is neutral.");
        }
        long matched = context.requirements().languages().stream().map(MatchingText::normalize).filter(resume::contains).count();
        int score = ScoringSupport.proportional(maximumScore, matched, context.requirements().languages().size());
        return new ScoreResult(dimension(), maximumScore, score,
                ScoringSupport.countReason("Languages", (int) matched, context.requirements().languages().size()));
    }
}
