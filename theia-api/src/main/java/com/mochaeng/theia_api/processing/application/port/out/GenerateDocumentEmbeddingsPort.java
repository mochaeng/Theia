package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.EmbeddingDocumentResult;
import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.shared.application.error.EmbeddingGenerationError;
import io.vavr.control.Either;

public interface GenerateDocumentEmbeddingsPort {
    Either<EmbeddingGenerationError, DocumentEmbeddings> generate(
        DocumentMetadata metadata
    );
}
