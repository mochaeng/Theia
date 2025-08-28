package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.domain.model.Document;

public interface UploadDocumentUseCase {
    void uploadDocument(Document document);
}
