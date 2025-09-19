package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.*;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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

        Try.run(() -> Thread.sleep(50_000));

        publishProgress.publish(
           DocumentProgressEvent.now(
               message.documentID(),
               "DOWNLOADING",
               "Start to downloading your file",
               10
           )
        );

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
