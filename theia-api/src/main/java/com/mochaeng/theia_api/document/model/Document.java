package com.mochaeng.theia_api.document.model;

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
        //        if (filename == null || filename.trim().isEmpty()) {
        //            throw new IllegalArgumentException("File must have a valid name");
        //        }
        //        if (!filename.contains(".")) {
        //            throw new IllegalArgumentException(
        //                "Filename must contain a valid extension"
        //            );
        //        }

        UUID id = UUID.randomUUID();
        String filename = id + ".pdf";

        return new Document(filename, contentType, id, content);
    }
}
