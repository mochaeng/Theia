package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.application.port.out.SaveDocumentPort;
import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.processing.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImplSaveDocument implements SaveDocumentPort {

    private final DocumentRepository documentRepository;

    @Override
    public void save(DocumentMetadata metadata, DocumentEmbeddings embeddings) {
        log.info(
            "saving document and embeddings for: {}",
            metadata.documentId()
        );

        var processedDocument = ProcessedDocument.from(metadata, embeddings);
        documentRepository.save(processedDocument);

        log.info(
            "Successfully saved document and embeddings for: {}",
            metadata.documentId()
        );
    }
}
