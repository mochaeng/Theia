package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;

public interface SaveDocumentPort {
    void save(DocumentMetadata metadata, DocumentEmbeddings embeddings);
}
