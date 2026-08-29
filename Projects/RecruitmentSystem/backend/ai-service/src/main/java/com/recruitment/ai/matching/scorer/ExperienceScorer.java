package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

@Component
public class ExperienceScorer implements MatchingScorer {
    @Override public String dimension() { return "experience"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        int required = context.requirements().minimumExperienceYears();
        String experience = MatchingText.fieldText(context.resumeFacts(), "experience");
        int actual = MatchingText.explicitYears(experience);
        if (actual == 0 && context.resumeFacts().path("experience").isArray()) {
            actual = context.resumeFacts().path("experience").size();
        }
        int score = required == 0 ? maximumScore : ScoringSupport.proportional(maximumScore, actual, required);
        return new ScoreResult(dimension(), maximumScore, score,
                "Kinh nghiệm nhận diện được: %d năm; yêu cầu của công việc: %d năm.".formatted(actual, required));
    }
}
