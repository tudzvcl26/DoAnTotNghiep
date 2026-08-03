package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

@Component
public class CertificateScorer implements MatchingScorer {
    @Override public String dimension() { return "certificates"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        boolean present = !MatchingText.fieldValues(context.resumeFacts(), "certificates").isEmpty();
        boolean required = context.requirements().certificateRequired();
        int score = !required || present ? maximumScore : 0;
        return new ScoreResult(dimension(), maximumScore, score,
                required ? (present ? "Certification evidence is present." : "The declared certification requirement is missing.")
                        : "No certification requirement is declared; this dimension is neutral.");
    }
}
