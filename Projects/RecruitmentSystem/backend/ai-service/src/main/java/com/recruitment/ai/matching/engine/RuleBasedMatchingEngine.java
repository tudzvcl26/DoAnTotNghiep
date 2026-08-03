package com.recruitment.ai.matching.engine;

import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.matching.model.MatchingComputation;
import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.model.ScoreResult;
import com.recruitment.ai.matching.rule.MatchingScorer;
import com.recruitment.ai.matching.util.MatchingText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RuleBasedMatchingEngine {

    private final MatchingProperties properties;
    private final Map<String, MatchingScorer> scorers;

    public RuleBasedMatchingEngine(MatchingProperties properties, List<MatchingScorer> scorers) {
        this.properties = properties;
        this.scorers = scorers.stream().collect(Collectors.toMap(
                MatchingScorer::dimension, Function.identity(), (first, second) -> first, LinkedHashMap::new));
        if (!this.scorers.keySet().containsAll(properties.getWeights().asMap().keySet())) {
            throw new IllegalStateException("A scorer is required for every configured matching dimension.");
        }
    }

    public MatchingComputation match(MatchingContext context) {
        List<ScoreResult> breakdown = new ArrayList<>();
        properties.getWeights().asMap().forEach((dimension, weight) ->
                breakdown.add(scorers.get(dimension).score(context, weight)));
        int overall = breakdown.stream().mapToInt(ScoreResult::actualScore).sum();

        Set<String> resumeSkills = resumeTerms(context, "technicalSkills", "skills");
        List<String> allJobSkills = new ArrayList<>(context.requirements().requiredSkills());
        allJobSkills.addAll(context.requirements().preferredSkills());
        List<String> matchedSkills = filter(allJobSkills, resumeSkills, true);
        List<String> missingSkills = filter(context.requirements().requiredSkills(), resumeSkills, false);

        Set<String> resumeKeywords = resumeTerms(context, "keywords", "technicalSkills", "skills");
        List<String> matchedKeywords = filter(context.requirements().keywords(), resumeKeywords, true);
        List<String> missingKeywords = filter(context.requirements().keywords(), resumeKeywords, false);

        List<String> strengths = breakdown.stream()
                .filter(score -> score.maximumScore() == 0 || score.actualScore() * 100 >= score.maximumScore() * 70)
                .map(score -> score.dimension() + ": " + score.reason()).toList();
        List<String> weaknesses = breakdown.stream()
                .filter(score -> score.maximumScore() > 0 && score.actualScore() * 100 < score.maximumScore() * 70)
                .map(score -> score.dimension() + ": " + score.reason()).toList();

        List<String> gaps = new ArrayList<>();
        if (!missingSkills.isEmpty()) gaps.add("Missing required skills: " + String.join(", ", missingSkills));
        if (!missingKeywords.isEmpty()) gaps.add("Missing job keywords: " + String.join(", ", missingKeywords));
        weaknesses.forEach(item -> gaps.add("Score gap: " + item));
        if (gaps.isEmpty()) gaps.add("No material rule-based gap was detected.");

        List<String> recommendations = new ArrayList<>();
        if (!missingSkills.isEmpty()) recommendations.add("Build or demonstrate the required skills: " + String.join(", ", missingSkills));
        if (!missingKeywords.isEmpty()) recommendations.add("Add truthful evidence for relevant keywords: " + String.join(", ", missingKeywords));
        breakdown.stream().filter(score -> score.actualScore() < score.maximumScore())
                .forEach(score -> recommendations.add("Improve " + score.dimension() + " evidence. " + score.reason()));
        if (recommendations.isEmpty()) recommendations.add("Maintain current evidence and keep the resume facts up to date.");

        String experience = breakdown.stream().filter(item -> item.dimension().equals("experience"))
                .findFirst().map(ScoreResult::reason).orElse("Experience was not evaluated.");
        String education = breakdown.stream().filter(item -> item.dimension().equals("education"))
                .findFirst().map(ScoreResult::reason).orElse("Education was not evaluated.");

        return new MatchingComputation(overall, List.copyOf(breakdown), matchedSkills, missingSkills,
                matchedKeywords, missingKeywords, strengths, weaknesses, List.copyOf(recommendations),
                List.copyOf(gaps), experience, education);
    }

    private Set<String> resumeTerms(MatchingContext context, String... fields) {
        Set<String> result = new LinkedHashSet<>();
        for (String field : fields) {
            result.addAll(MatchingText.normalized(MatchingText.fieldValues(context.resumeFacts(), field)));
        }
        return result;
    }

    private List<String> filter(List<String> candidates, Set<String> normalizedResume, boolean matched) {
        return candidates.stream().filter(value -> normalizedResume.contains(MatchingText.normalize(value)) == matched)
                .distinct().toList();
    }
}
