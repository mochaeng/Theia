package com.mochaeng.theia_api.shared.domain;

public class TextNormalizer {

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

        if (normalized.length() >= maxChars) {
            normalized = normalized.substring(0, maxChars);
        }

        return normalized;
    }
}
