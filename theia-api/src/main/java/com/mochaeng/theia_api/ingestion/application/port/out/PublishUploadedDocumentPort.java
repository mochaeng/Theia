package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;

public interface PublishUploadedDocumentPort {
    void publish(DocumentUploadedMessage event);
}
