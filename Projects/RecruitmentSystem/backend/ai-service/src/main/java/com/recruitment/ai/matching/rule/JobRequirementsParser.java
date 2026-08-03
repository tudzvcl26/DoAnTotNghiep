package com.recruitment.ai.matching.rule;

import com.recruitment.ai.config.MatchingProperties;
import com.recruitment.ai.matching.model.JobRequirements;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.matching.util.MatchingText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JobRequirementsParser {

    private static final Pattern YEARS = Pattern.compile("(?i)(\\d{1,2})\\s*\\+?\\s*(?:years?|yrs?)");
    private static final Set<String> STOP_WORDS = Set.of(
            "and", "the", "with", "for", "you", "your", "our", "are", "will", "from", "have", "has",
            "job", "work", "team", "role", "skills", "skill", "requirements", "responsibilities", "candidate",
            "experience", "years", "year", "using", "knowledge", "strong", "good", "ability", "required"
    );

    private final MatchingProperties properties;

    public JobRequirements parse(JobSnapshot job) {
        String text = job.searchableText();
        List<String> required = new ArrayList<>();
        List<String> preferred = new ArrayList<>();
        for (String skill : properties.getTechnicalSkillCatalog()) {
            if (MatchingText.contains(text, skill)) {
                if (isPreferred(text, skill)) {
                    preferred.add(skill);
                } else {
                    required.add(skill);
                }
            }
        }
        List<String> softSkills = presentCatalog(text, properties.getSoftSkillCatalog());
        List<String> languages = presentCatalog(text, properties.getLanguageCatalog());
        int years = minimumYears(text);
        if (years == 0) {
            years = experienceLevelYears(job.experienceLevel());
        }
        String normalized = MatchingText.normalize(text);
        boolean degreeRequired = containsAny(normalized, "bachelor", "master", "degree", "university", "college");
        boolean certificateRequired = containsAny(normalized, "certificate", "certification", "certified");
        return new JobRequirements(
                List.copyOf(required), List.copyOf(preferred), List.copyOf(softSkills), List.copyOf(languages),
                keywords(text), years, degreeRequired, certificateRequired
        );
    }

    private List<String> presentCatalog(String text, List<String> catalog) {
        return catalog.stream().filter(item -> MatchingText.contains(text, item)).toList();
    }

    private boolean isPreferred(String text, String skill) {
        String normalized = MatchingText.normalize(text);
        String term = MatchingText.normalize(skill);
        int index = normalized.indexOf(term);
        if (index < 0) {
            return false;
        }
        String prefix = normalized.substring(Math.max(0, index - 45), index);
        return containsAny(prefix, "preferred", "nice to have", "bonus", "optional", "plus");
    }

    private int minimumYears(String text) {
        Matcher matcher = YEARS.matcher(text);
        int result = 0;
        while (matcher.find()) {
            result = Math.max(result, Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    private int experienceLevelYears(String level) {
        if (level == null) return 0;
        return switch (level.toUpperCase(Locale.ROOT)) {
            case "FRESHER" -> 0;
            case "JUNIOR" -> 1;
            case "MIDDLE" -> 3;
            case "SENIOR" -> 5;
            case "LEADER" -> 6;
            case "MANAGER" -> 7;
            default -> 0;
        };
    }

    private List<String> keywords(String text) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Arrays.stream(MatchingText.normalize(text).split(" "))
                .filter(token -> token.length() >= 3 && !STOP_WORDS.contains(token))
                .limit(40)
                .forEach(result::add);
        return List.copyOf(result);
    }

    private boolean containsAny(String value, String... needles) {
        return Arrays.stream(needles).anyMatch(value::contains);
    }
}
