package com.recruitment.ai.matching.rule;

import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;

public interface MatchingScorer {
    String dimension();
    ScoreResult score(MatchingContext context, int maximumScore);
}
