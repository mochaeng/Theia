package com.mochaeng.theia_api.processing.application.port.in;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;

public interface ProcessDocumentUseCase {
    void process(DocumentMessage message);
}
