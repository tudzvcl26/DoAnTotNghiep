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
                ? (present ? "Công việc có yêu cầu bằng cấp và CV đã cung cấp thông tin học vấn."
                : "Công việc có yêu cầu bằng cấp nhưng CV chưa cung cấp thông tin học vấn.")
                : "Công việc chưa nêu yêu cầu tối thiểu về bằng cấp; ứng viên không bị trừ điểm ở tiêu chí này.";
        return new ScoreResult(dimension(), maximumScore, score, reason);
    }
}
