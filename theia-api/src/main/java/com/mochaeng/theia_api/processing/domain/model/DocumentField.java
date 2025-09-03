package com.mochaeng.theia_api.processing.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentField {
    TITLE("title"),
    ABSTRACT("abstract");

    private final String fieldName;

    @Override
    public String toString() {
        return fieldName;
    }
}
