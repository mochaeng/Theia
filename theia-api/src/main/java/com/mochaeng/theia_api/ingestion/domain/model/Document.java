package com.mochaeng.theia_api.ingestion.domain.model;

import java.util.UUID;

public record Document(
    String filename,
    String contentType,
    UUID id,
    byte[] content
) {
    public Document {
        content = content == null ? null : content.clone();
    }

    public byte[] content() {
        return content == null ? null : content.clone();
    }

    public static Document create(String contentType, byte[] content) {
        UUID id = UUID.randomUUID();
        String filename = id + ".pdf";

        return new Document(filename, contentType, id, content);
    }
}
