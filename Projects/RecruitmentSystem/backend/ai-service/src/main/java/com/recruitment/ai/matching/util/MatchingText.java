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

    private static final Pattern YEARS = Pattern.compile("(?i)(\\d{1,2})\\s*\\+?\\s*(?:years?|yrs?)");

    private MatchingText() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#.]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    public static boolean contains(String text, String term) {
        String normalizedText = " " + normalize(text) + " ";
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

    public static List<String> fieldValues(JsonNode root, String field) {
        List<String> values = new ArrayList<>();
        JsonNode node = root.path(field);
        collect(node, values);
        return values;
    }

    public static String fieldText(JsonNode root, String field) {
        return String.join(" ", fieldValues(root, field));
    }

    public static int explicitYears(String text) {
        int maximum = 0;
        Matcher matcher = YEARS.matcher(text == null ? "" : text);
        while (matcher.find()) {
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
