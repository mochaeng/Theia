package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.PersistenceDocumentResult;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;

public interface DocumentPersistencePort {
    PersistenceDocumentResult save(ProcessedDocument document);
}
