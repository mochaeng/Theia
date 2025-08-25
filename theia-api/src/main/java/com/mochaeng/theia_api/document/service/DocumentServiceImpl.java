package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.document.dto.DocumentUploadedEvent;
import com.mochaeng.theia_api.document.model.Document;
import com.mochaeng.theia_api.shared.config.kafka.KafkaEventPublisher;
import com.mochaeng.theia_api.storage.s3.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentValidationService validationService;
    private final StorageService storageService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    public void uploadDocument(Document document) {
        validationService.validateDocument(document);

        String s3Path = storageService.storeDocument(document);

        DocumentUploadedEvent event = DocumentUploadedEvent.create(
            document.id(),
            document.filename(),
            s3Path,
            document.contentType(),
            document.content().length
        );

        kafkaEventPublisher.publishDocumentUploadedEvent(event);

        log.info(
            "Document uploaded process completed for: {} with id: {}",
            document.filename(),
            document.id()
        );
    }
}
