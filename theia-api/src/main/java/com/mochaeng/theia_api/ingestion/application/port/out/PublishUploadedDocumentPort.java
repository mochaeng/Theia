package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.shared.application.events.DocumentUploadedEvent;

public interface PublishUploadedDocumentPort {
    void publish(DocumentUploadedEvent event);
}
