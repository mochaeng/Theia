package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.DocumentPersistencePort;
import com.mochaeng.theia_api.processing.application.port.out.DownloadDocumentPort;
import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingsPort;
import com.mochaeng.theia_api.processing.domain.model.FieldEmbedding;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.util.Arrays;
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
            // publish failed download event to kafka
            return;
        }

        var metadataResult = extractDocumentData.extract(
            message,
            downloadResult.content()
        );
        if (!metadataResult.isSuccess()) {
            // publish failed extract event to kafka
            return;
        }

        log.info("Metadata: {}", metadataResult.metadata());

        var embeddingsResult = generateDocumentEmbeddings.generate(
            metadataResult.metadata()
        );
        if (!embeddingsResult.isSuccess()) {
            // publish failed embeddings event to kafka
            return;
        }

        log.info("Embeddings generated successfully");
        //        for (FieldEmbedding fieldEmbedding : embeddingsResult
        //            .embedding()
        //            .fieldEmbeddings()) {
        //            log.info(
        //                "Field: {}, Embedding: {}",
        //                fieldEmbedding.fieldName(),
        //                Arrays.toString(fieldEmbedding.embedding())
        //            );
        //        }

        var processedDocument = ProcessedDocument.from(
            metadataResult.metadata(),
            downloadResult.hash(),
            message.bucketPath(),
            embeddingsResult.embedding()
        );

        log.info("document to be processed: {}", processedDocument);

        documentPersistence.save(processedDocument);
    }
}
