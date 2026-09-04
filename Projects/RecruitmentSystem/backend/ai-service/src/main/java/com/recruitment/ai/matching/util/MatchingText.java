package com.recruitment.ai.matching.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MatchingText {

    private static final Pattern YEARS = Pattern.compile("(?i)(?<!\\d)(\\d{1,2})\\s*(?:[-–]\\s*\\d{1,2})?\\s*\\+?\\s*(?:years?|yrs?|năm|nam)\\b");

    private MatchingText() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.ROOT).replace('đ', 'd').replaceAll("[^a-z0-9+#.]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    public static boolean contains(String text, String term) {
        // Sentence punctuation is not part of a skill; retain internal dots in Node.js.
        String normalizedText = " " + normalize(text).replaceAll("\\.(?=\\s|$)", "") + " ";
        String normalizedTerm = normalize(term);
        return !normalizedTerm.isBlank() && normalizedText.contains(" " + normalizedTerm + " ");
    }

    public static Set<String> normalized(Collection<String> values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    public static String canonicalSkill(String value) {
        String skill = normalize(value).replaceAll("\\s+(?:co ban|basics?|beginner)$", "")
                .replaceAll("^basic\\s+", "");
        return switch (skill) {
            case "rest api", "rest apis", "restful", "restful api", "restful apis" -> "rest";
            case "nodejs", "node js" -> "node.js";
            case "postgres" -> "postgresql";
            case "js" -> "javascript";
            case "ts" -> "typescript";
            default -> skill;
        };
    }

    public static Set<String> normalizedSkills(Collection<String> values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) { String skill = canonicalSkill(value); if (!skill.isBlank()) result.add(skill); }
        return result;
    }

    public static List<String> fieldValues(JsonNode root, String field) {
        List<String> values = new ArrayList<>();
        JsonNode node = root.path(field);
        collect(node, values);
        return values;
    }

    public static boolean isExplicitAbsence(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() || Set.of("none", "n a", "null", "undefined", "not provided", "not applicable").contains(normalized)
                || normalized.matches("^(?:khong co|chua co|khong|no|without)\\b.*");
    }

    public static boolean hasCertificateEvidence(JsonNode node) {
        if (node == null || node.isNull()) return false;
        if (node.isTextual()) return !isExplicitAbsence(node.asText());
        if (node.isArray()) {
            for (JsonNode item : node) if (hasCertificateEvidence(item)) return true;
            return false;
        }
        if (node.isObject()) {
            // An issuer/date alone must not override an explicit absent name.
            for (String key : List.of("name", "certificateName", "certificate", "title")) {
                if (node.has(key)) return hasCertificateEvidence(node.path(key));
            }
            List<String> values = new ArrayList<>();
            collect(node, values);
            return values.stream().anyMatch(value -> !isExplicitAbsence(value));
        }
        return false;
    }

    public static String fieldText(JsonNode root, String field) {
        return String.join(" ", fieldValues(root, field));
    }

    public static int explicitYears(String text) {
        int maximum = 0;
        String duration = text == null ? "" : text.toLowerCase(Locale.ROOT);
        String[] numbers = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười"};
        for (int i = 0; i < numbers.length; i++) duration = duration.replaceAll("(?U)\\b" + numbers[i] + "\\s+năm\\b", i + " năm");
        Matcher matcher = YEARS.matcher(duration);
        while (matcher.find()) {
            String prefix = normalize(duration.substring(0, matcher.start()));
            // "tháng 7 năm 2024" is a calendar date, not seven years' experience.
            if (prefix.equals("thang") || prefix.endsWith(" thang")) continue;
            maximum = Math.max(maximum, Integer.parseInt(matcher.group(1)));
        }
        return maximum;
    }

    private static void collect(JsonNode node, List<String> values) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            if (!node.asText().isBlank()) {
                values.add(node.asText());
            }
            return;
        }
        node.elements().forEachRemaining(child -> collect(child, values));
    }
}
