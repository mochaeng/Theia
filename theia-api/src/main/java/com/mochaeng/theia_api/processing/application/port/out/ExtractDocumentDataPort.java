package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.ExtractDocumentResult;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;

public interface ExtractDocumentDataPort {
    ExtractDocumentResult extract(
        DocumentUploadedMessage message,
        byte[] documentBytes
    );
}
