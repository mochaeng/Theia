package com.mochaeng.theia_api.shared.domain;

public class TextNormalizer {

    public static String clean(String text) {
        return clean(text, 10_000);
    }

    public static String clean(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return "";
        }

        var normalized = text.replaceAll("\\p{Cntrl}", "").trim();

        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.length() >= maxChars) {
            normalized = normalized.substring(0, maxChars);
        }

        return normalized;
    }

    public static String forEmbedding(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return "";
        }

        var normalized = text
            .replaceAll("[^\\p{Print}]", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();

        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.length() >= maxChars) {
            normalized = normalized.substring(0, maxChars);
        }

        return normalized;
    }
}
