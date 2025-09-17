package com.mochaeng.theia_api.query.application.service.errors;

import com.mochaeng.theia_api.query.application.error.RetrieveDocumentError;
import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import java.util.function.Function;

public sealed interface DocumentSearchError {
    String message();

    record InvalidInputError(String message) implements DocumentSearchError {}

    record GenerateQueryEmbeddingError(String message) implements
        DocumentSearchError {}

    record RetrieveSimilarItemsError(String message) implements
        DocumentSearchError {}

    record UnknownError(String message) implements DocumentSearchError {}

    static <T> Function<T, DocumentSearchError> toDomainError() {
        return err ->
            switch (err) {
                case EmbeddingError e -> new DocumentSearchError.GenerateQueryEmbeddingError(
                    "Failed to generate embedding for query search: %s".formatted(
                        e.message()
                    )
                );
                case RetrieveDocumentError e -> new DocumentSearchError.RetrieveSimilarItemsError(
                    "Failed to retrieve similar documents for query search: %s".formatted(
                        e.message()
                    )
                );
                default -> new DocumentSearchError.UnknownError(
                    "Unknow error during document search"
                );
            };
    }
}
