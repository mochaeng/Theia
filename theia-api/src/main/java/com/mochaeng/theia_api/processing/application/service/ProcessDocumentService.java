package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.*;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessDocumentService implements ProcessDocumentUseCase {

    private final DownloadDocumentPort downloader;
    private final ExtractDocumentDataPort extractor;
    private final GenerateDocumentEmbeddingsPort embedding;
    private final DocumentPersistencePort documentPersistence;
    private final PublishProgressDocumentEventPort publisher;

    @Override
    public void process(IncomingDocumentMessage message) {
        log.info("processing uploaded document message event: {}", message);

        publisher.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.DOWNLOADING
            )
        );

        var downloadResult = downloader.download(message);
        if (!downloadResult.isSuccess()) {
            publishFailedEvent(message.documentID());
            return;
        }

        publisher.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.EXTRACTING
            )
        );

        var metadataResult = extractor.extract(
            message.documentID(),
            downloadResult.content()
        );
        if (!metadataResult.isSuccess()) {
            log.info("extraction failed: {}", metadataResult.errorMessage());
            publishFailedEvent(message.documentID());
            return;
        }
        log.info(
            "metadata extracted for document with id [{}]: {}",
            message.documentID(),
            metadataResult.metadata()
        );

        publisher.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.EMBEDDING
            )
        );

        var embeddings = embedding.generate(metadataResult.metadata());
        if (embeddings.isLeft()) {
            log.info(
                "failed to generate embeddings for [{}]",
                message.documentID()
            );
            publishFailedEvent(message.documentID());
            return;
        }

        log.info("embeddings generated successfully");

        publisher.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.SAVING
            )
        );

        var processedDocument = ProcessedDocument.from(
            metadataResult.metadata(),
            downloadResult.hash(),
            //            message.bucketPath(),
            "",
            embeddings.get()
        );
        log.info("document to be saved: {}", processedDocument);
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
            "document with id [{}] was persisted successfully",
            processedDocument.id()
        );

        publisher.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.COMPLETED
            )
        );
    }

    private void publishFailedEvent(UUID documentID) {
        publisher.publish(
            DocumentProgressEvent.now(
                documentID,
                DocumentProgressEvent.Status.FAILED
            )
        );
    }
}
