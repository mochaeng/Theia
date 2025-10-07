package com.mochaeng.theia_api.processing.application.port.in;

import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;

public interface ProcessDocumentUseCase {
    void process(IncomingDocumentMessage message);
}
