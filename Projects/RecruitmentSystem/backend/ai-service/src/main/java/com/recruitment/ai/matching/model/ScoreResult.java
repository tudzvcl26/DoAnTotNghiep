package com.recruitment.ai.matching.model;

public record ScoreResult(String dimension, int maximumScore, int actualScore, String reason) {
    public ScoreResult {
        if (maximumScore < 0 || actualScore < 0 || actualScore > maximumScore) {
            throw new IllegalArgumentException("Invalid matching score result.");
        }
    }
}
