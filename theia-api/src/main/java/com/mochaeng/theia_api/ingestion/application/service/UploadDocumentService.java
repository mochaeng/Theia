package com.mochaeng.theia_api.ingestion.application.service;

import com.mochaeng.theia_api.ingestion.application.port.in.UploadDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.in.ValidateDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.out.FileStoragePort;
import com.mochaeng.theia_api.ingestion.application.port.out.PublishUploadedDocumentPort;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadDocumentService implements UploadDocumentUseCase {

    private final ValidateDocumentUseCase validateDocument;
    private final FileStoragePort fileStorage;
    private final PublishUploadedDocumentPort publishUploadedDocumentEvent;

    @Override
    public void uploadDocument(Document document) {
        validateDocument.validate(document);

        String s3Path = fileStorage.storeDocument(document);

        DocumentUploadedMessage event = DocumentUploadedMessage.create(
            document.id(),
            document.filename(),
            s3Path,
            document.contentType(),
            Objects.requireNonNull(document.content()).length
        );
        publishUploadedDocumentEvent.publish(event);

        log.info(
            "Document uploaded process completed for: {} with id: {}",
            document.filename(),
            document.id()
        );
    }
}
