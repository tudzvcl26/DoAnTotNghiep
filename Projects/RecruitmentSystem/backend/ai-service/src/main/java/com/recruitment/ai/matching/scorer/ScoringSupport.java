package com.recruitment.ai.matching.scorer;

import java.util.Collection;

final class ScoringSupport {
    private ScoringSupport() {
    }

    static int proportional(int maximum, double numerator, double denominator) {
        if (denominator <= 0) return maximum;
        return Math.max(0, Math.min(maximum, (int) Math.round(maximum * numerator / denominator)));
    }

    static String countReason(String label, int matched, int required) {
        return "%s: %d of %d matched.".formatted(label, matched, required);
    }

    static boolean hasValues(Collection<String> values) {
        return values != null && values.stream().anyMatch(value -> value != null && !value.isBlank());
    }
}
