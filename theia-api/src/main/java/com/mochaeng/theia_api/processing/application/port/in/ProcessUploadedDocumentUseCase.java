package com.mochaeng.theia_api.processing.application.port.in;

import com.mochaeng.theia_api.shared.application.events.DocumentUploadedEvent;

public interface ProcessUploadedDocumentUseCase {
    void process(DocumentUploadedEvent event);
}
