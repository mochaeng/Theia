package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.DocumentPersistencePort;
import com.mochaeng.theia_api.processing.application.port.out.DownloadDocumentPort;
import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingsPort;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessDocumentService implements ProcessDocumentUseCase {

    private final DownloadDocumentPort downloadDocument;
    private final ExtractDocumentDataPort extractDocumentData;
    private final GenerateDocumentEmbeddingsPort generateDocumentEmbeddings;
    private final DocumentPersistencePort documentPersistence;

    @Override
    public void process(DocumentUploadedMessage message) {
        log.info("processing uploaded document message event: {}", message);

        var downloadResult = downloadDocument.download(message);
        if (!downloadResult.isSuccess()) {
            return;
        }

        var metadataResult = extractDocumentData.extract(
            message.documentID(),
            downloadResult.content()
        );
        if (!metadataResult.isSuccess()) {
            log.info("extraction failed: {}", metadataResult.errorMessage());
            return;
        }
        log.info(
            "metadata extracted for document with id [{}]: {}",
            message.documentID(),
            metadataResult.metadata()
        );

        var embeddings = generateDocumentEmbeddings.generate(
            metadataResult.metadata()
        );
        if (embeddings.isLeft()) {
            return;
        }

        log.info("embeddings generated successfully");

        var processedDocument = ProcessedDocument.from(
            metadataResult.metadata(),
            downloadResult.hash(),
            message.bucketPath(),
            embeddings.get()
        );
        log.info("document to be saved: {}", processedDocument);
        var persistenceResult = documentPersistence.save(processedDocument);
        if (!persistenceResult.isSuccess()) {
            // publish failed persistence event to kafka
            return;
        }
        log.info(
            "document with id [{}] was persisted successfully",
            processedDocument.id()
        );
    }
}
