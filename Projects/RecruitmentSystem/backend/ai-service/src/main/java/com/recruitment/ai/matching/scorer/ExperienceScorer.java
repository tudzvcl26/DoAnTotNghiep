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
        String summary = MatchingText.fieldText(context.resumeFacts(), "summary");
        int summaryYears = MatchingText.contains(summary, "experience") || MatchingText.contains(summary, "kinh nghiệm")
                ? MatchingText.explicitYears(summary) : 0;
        int actual = Math.max(MatchingText.explicitYears(experience), summaryYears);
        int score = required == 0 ? maximumScore : ScoringSupport.proportional(maximumScore, actual, required);
        if (actual == 0) return new ScoreResult(dimension(), maximumScore, score,
                "Chưa đủ dữ liệu về số năm kinh nghiệm trong CV; không suy ra số năm từ số mục công việc. Yêu cầu: %d năm.".formatted(required));
        return new ScoreResult(dimension(), maximumScore, score,
                "Kinh nghiệm nhận diện được: %d năm; yêu cầu của công việc: %d năm.".formatted(actual, required));
    }
}
