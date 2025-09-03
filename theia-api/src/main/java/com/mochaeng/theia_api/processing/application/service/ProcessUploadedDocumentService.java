package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.application.port.in.ProcessUploadedDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.DownloadDocumentPort;
import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingPort;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessUploadedDocumentService
    implements ProcessUploadedDocumentUseCase {

    private final DownloadDocumentPort downloadDocument;
    private final ExtractDocumentDataPort extractDocumentData;
    private final GenerateDocumentEmbeddingPort generateDocumentEmbedding;

    @Override
    public void process(DocumentUploadedMessage message) {
        log.info("Processing uploaded document event");

        var downloadResult = downloadDocument.download(message);
        if (!downloadResult.isSuccess()) {
            // publish failed download event to kafka
            return;
        }

        var documentDataResult = extractDocumentData.extract(
            message,
            downloadResult.content()
        );
        if (!documentDataResult.isSuccess()) {
            // publish failed extract event to kafka
            return;
        }

        log.info("Metadata: {}", documentDataResult.metadata());

        var embeddingsResult = generateDocumentEmbedding.generate(
            documentDataResult.metadata()
        );
        if (!embeddingsResult.isSuccess()) {
            // publish failed embeddings event to kafka
            return;
        }

        log.info("Embeddings generated successfully");
        log.info(embeddingsResult.embedding().fieldEmbeddings().toString());
    }
}
