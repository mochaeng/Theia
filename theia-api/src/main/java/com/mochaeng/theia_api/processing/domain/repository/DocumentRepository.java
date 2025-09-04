package com.mochaeng.theia_api.processing.domain.repository;

import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    ProcessedDocument save(ProcessedDocument document);
    Optional<ProcessedDocument> findById(UUID id);
    Boolean existsById(UUID id);
    void deleteById(UUID id);
}
