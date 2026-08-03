package com.recruitment.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.matching")
public class MatchingProperties {

    private String ruleVersion = "rules-v1";
    private String weightsVersion = "weights-v1";
    private Weights weights = new Weights();
    private List<String> technicalSkillCatalog = List.of();
    private List<String> softSkillCatalog = List.of();
    private List<String> languageCatalog = List.of();

    @PostConstruct
    void validate() {
        if (ruleVersion == null || ruleVersion.isBlank() || weightsVersion == null || weightsVersion.isBlank()) {
            throw new IllegalStateException("Matching rule and weights versions are required.");
        }
        int total = weights.asMap().values().stream().mapToInt(Integer::intValue).sum();
        if (total != 100 || weights.asMap().values().stream().anyMatch(value -> value < 0)) {
            throw new IllegalStateException("Matching weights must be non-negative and total 100.");
        }
    }

    @Getter
    @Setter
    public static class Weights {
        private int technicalSkills = 40;
        private int experience = 20;
        private int education = 10;
        private int projects = 10;
        private int certificates = 5;
        private int languages = 5;
        private int softSkills = 5;
        private int keywords = 5;

        public Map<String, Integer> asMap() {
            Map<String, Integer> result = new LinkedHashMap<>();
            result.put("technicalSkills", technicalSkills);
            result.put("experience", experience);
            result.put("education", education);
            result.put("projects", projects);
            result.put("certificates", certificates);
            result.put("languages", languages);
            result.put("softSkills", softSkills);
            result.put("keywords", keywords);
            return result;
        }
    }
}
