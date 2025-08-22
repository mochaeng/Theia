package com.mochaeng.theia_api.document.model;

import java.util.UUID;

public record Document(
    String filename,
    String contentType,
    UUID documentId,
    byte[] content
) {
    public static Document create(String filename, String contentType, byte[] content) {
        return new Document(filename, contentType, UUID.randomUUID(), content);
    }
}
