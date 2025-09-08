package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.EmbeddingDocumentResult;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;

public interface GenerateDocumentEmbeddingsPort {
    EmbeddingDocumentResult generate(DocumentMetadata metadata);
}
