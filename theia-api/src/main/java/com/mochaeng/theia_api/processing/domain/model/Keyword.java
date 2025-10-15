package com.mochaeng.theia_api.processing.domain.model;

import lombok.Builder;

@Builder
public record Keyword(String value) {
    public Keyword {
        value = value == null ? "" : value.trim();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public static Keyword empty() {
        return Keyword.builder().value("").build();
    }
}
