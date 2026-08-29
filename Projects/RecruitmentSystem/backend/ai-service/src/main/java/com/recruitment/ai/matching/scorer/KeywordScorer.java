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
public class KeywordScorer implements MatchingScorer {
    @Override public String dimension() { return "keywords"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        List<String> resumeValues = new ArrayList<>(MatchingText.fieldValues(context.resumeFacts(), "keywords"));
        resumeValues.addAll(MatchingText.fieldValues(context.resumeFacts(), "technicalSkills"));
        resumeValues.addAll(MatchingText.fieldValues(context.resumeFacts(), "skills"));
        Set<String> resume = MatchingText.normalized(resumeValues);
        List<String> job = context.requirements().keywords();
        if (job.isEmpty()) {
            return new ScoreResult(dimension(), maximumScore, maximumScore, "Chưa tìm thấy từ khóa công việc đủ rõ để đánh giá; ứng viên không bị trừ điểm ở tiêu chí này.");
        }
        long matched = job.stream().map(MatchingText::normalize).filter(resume::contains).count();
        int score = ScoringSupport.proportional(maximumScore, matched, job.size());
        return new ScoreResult(dimension(), maximumScore, score, ScoringSupport.countReason("Từ khóa công việc", (int) matched, job.size()));
    }
}
