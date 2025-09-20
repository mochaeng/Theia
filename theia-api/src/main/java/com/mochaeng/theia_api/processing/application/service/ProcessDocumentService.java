package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.*;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.util.UUID;
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
    private final PublishProgressDocumentEventPort publishProgress;

    @Override
    public void process(DocumentUploadedMessage message) {
        log.info("processing uploaded document message event: {}", message);

        publishProgress.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.DOWNLOADING
            )
        );

        var downloadResult = downloadDocument.download(message);
        if (!downloadResult.isSuccess()) {
            publishFailedEvent(message.documentID());
            return;
        }

        publishProgress.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.EXTRACTING
            )
        );

        var metadataResult = extractDocumentData.extract(
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

        publishProgress.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.EMBEDDING
            )
        );

        var embeddings = generateDocumentEmbeddings.generate(
            metadataResult.metadata()
        );
        if (embeddings.isLeft()) {
            log.info(
                "failed to generate embeddings for [{}]",
                message.documentID()
            );
            publishFailedEvent(message.documentID());
            return;
        }

        log.info("embeddings generated successfully");

        publishProgress.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.SAVING
            )
        );

        var processedDocument = ProcessedDocument.from(
            metadataResult.metadata(),
            downloadResult.hash(),
            message.bucketPath(),
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

        publishProgress.publish(
            DocumentProgressEvent.now(
                message.documentID(),
                DocumentProgressEvent.Status.COMPLETED
            )
        );
    }

    private void publishFailedEvent(UUID documentID) {
        publishProgress.publish(
            DocumentProgressEvent.now(
                documentID,
                DocumentProgressEvent.Status.FAILED
            )
        );
    }
}
