package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import io.vavr.control.Either;

public interface GenerateDocumentEmbeddingsPort {
    Either<EmbeddingError, DocumentEmbeddings> generate(
        DocumentMetadata metadata
    );
}
