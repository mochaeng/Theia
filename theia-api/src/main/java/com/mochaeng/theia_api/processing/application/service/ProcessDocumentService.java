package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.*;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessDocumentService implements ProcessDocumentUseCase {

    private final FileStorage downloader;
    private final ExtractDocumentDataPort extractor;
    private final GenerateDocumentEmbeddingsPort embedding;
    private final DocumentPersistencePort documentPersistence;
    private final PublishProgressDocumentEventPort publisher;

    @Override
    public void process(DocumentMessage message) {
        log.info("processing valid-document '{}'", message.documentID());

        transitionTo(
            message.documentID(),
            DocumentProgressEvent.Status.DOWNLOADING
        );

        var downloadResult = downloader.download(message);
        if (downloadResult.isLeft()) {
            publishFailedEvent(message.documentID());
            return;
        }

        transitionTo(
            message.documentID(),
            DocumentProgressEvent.Status.EXTRACTING
        );

        var metadataResult = extractor.extract(
            message.documentID(),
            downloadResult.get().content()
        );
        if (!metadataResult.isSuccess()) {
            log.info("extraction failed: {}", metadataResult.errorMessage());
            publishFailedEvent(message.documentID());
            return;
        }
        log.info(
            "metadata extracted for '{}': {}",
            message.documentID(),
            metadataResult.metadata()
        );

        transitionTo(
            message.documentID(),
            DocumentProgressEvent.Status.EMBEDDING
        );

        var embeddings = embedding.generate(
            message.documentID(),
            metadataResult.metadata()
        );
        if (embeddings.isLeft()) {
            log.info(
                "embedding generation failed for '{}': {}",
                message.documentID(),
                embeddings.getLeft().message()
            );
            publishFailedEvent(message.documentID());
            return;
        }

        log.info("embeddings generated successfully");

        transitionTo(message.documentID(), DocumentProgressEvent.Status.SAVING);

        var filePath = "%s/%s".formatted(message.bucket(), message.key());
        var processedDocument = ProcessedDocument.from(
            message.documentID(),
            metadataResult.metadata(),
            downloadResult.get().hash(),
            filePath,
            embeddings.get()
        );
        log.info("document '{}' saved", processedDocument);
        var persistenceResult = documentPersistence.save(processedDocument);
        if (!persistenceResult.isSuccess()) {
            log.info(
                "failed to persist document with id [{}]",
                message.documentID()
            );
            publishFailedEvent(message.documentID());
            return;
        }
        log.info(
            "document '{}' was persisted successfully",
            processedDocument.id()
        );

        transitionTo(
            message.documentID(),
            DocumentProgressEvent.Status.COMPLETED
        );
    }

    private void transitionTo(
        UUID documentID,
        DocumentProgressEvent.Status status
    ) {
        log.info("document '{}' transitioning to '{}'", documentID, status);
        publisher.publish(DocumentProgressEvent.now(documentID, status));
    }

    private void publishFailedEvent(UUID documentID) {
        transitionTo(documentID, DocumentProgressEvent.Status.FAILED);
    }
}
