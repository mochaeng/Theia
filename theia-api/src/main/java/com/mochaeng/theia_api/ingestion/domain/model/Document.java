package com.mochaeng.theia_api.ingestion.domain.model;

import io.vavr.control.Either;
import java.util.UUID;
import org.springframework.http.MediaType;

public record Document(
    String filename,
    String contentType,
    UUID id,
    String userID,
    byte[] content
) {
    public Document {
        content = content == null ? null : content.clone();
    }

    public byte[] content() {
        return content == null ? new byte[0] : content.clone();
    }

    public static Either<String, Document> create(
        String contentType,
        String userID,
        byte[] content
    ) {
        if (contentType == null || contentType.isEmpty()) {
            return Either.left("invalid content type");
        }

        if (!contentType.equals(MediaType.APPLICATION_PDF_VALUE)) {
            return Either.left("invalid content type");
        }

        UUID id = UUID.randomUUID();
        String filename = id + ".pdf";

        return Either.right(
            new Document(filename, contentType, id, userID, content)
        );
    }
}
