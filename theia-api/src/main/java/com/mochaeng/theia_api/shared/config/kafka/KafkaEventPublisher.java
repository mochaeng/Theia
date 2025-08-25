package com.mochaeng.theia_api.shared.config.kafka;

import com.mochaeng.theia_api.document.dto.DocumentFailedEvent;
import com.mochaeng.theia_api.document.dto.DocumentProcessedEvent;
import com.mochaeng.theia_api.document.dto.DocumentUploadedEvent;

public interface KafkaEventPublisher {
    void publishDocumentUploadedEvent(DocumentUploadedEvent event);
    void publishDocumentProcessedEvent(DocumentProcessedEvent event);
    void publishDocumentFailedEvent(DocumentFailedEvent event);
}
