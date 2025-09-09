package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.application.dto.PersistenceDocumentResult;
import com.mochaeng.theia_api.processing.application.port.out.DocumentPersistencePort;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ImplDocumentPersistence implements DocumentPersistencePort {

    private final DocumentPersistenceService documentService;

    @Override
    public PersistenceDocumentResult save(ProcessedDocument document) {
        return documentService.persistDocument(document);
    }
}
