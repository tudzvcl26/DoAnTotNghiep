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
                required ? (present ? "CV đã cung cấp thông tin chứng chỉ." : "CV chưa thể hiện chứng chỉ mà công việc yêu cầu.")
                        : "Công việc chưa nêu yêu cầu chứng chỉ; ứng viên không bị trừ điểm ở tiêu chí này.");
    }
}
