package com.recruitment.ai.matching.scorer;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectScorer implements MatchingScorer {
    @Override public String dimension() { return "projects"; }

    @Override
    public ScoreResult score(MatchingContext context, int maximumScore) {
        String projects = MatchingText.fieldText(context.resumeFacts(), "projects");
        if (projects.isBlank()) {
            return new ScoreResult(dimension(), maximumScore, 0, "No project evidence is present.");
        }
        List<String> targets = new ArrayList<>(context.requirements().requiredSkills());
        targets.addAll(context.requirements().preferredSkills());
        if (targets.isEmpty()) {
            return new ScoreResult(dimension(), maximumScore, maximumScore, "Project evidence is present; no technical project target is declared.");
        }
        long matched = targets.stream().filter(term -> MatchingText.contains(projects, term)).count();
        int score = ScoringSupport.proportional(maximumScore, matched, targets.size());
        return new ScoreResult(dimension(), maximumScore, score, ScoringSupport.countReason("Project technology coverage", (int) matched, targets.size()));
    }
}
