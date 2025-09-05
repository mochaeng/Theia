package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.processing.domain.repository.DocumentRepository;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.DocumentEntity;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.JpaDocumentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImplDocumentRepository implements DocumentRepository {

    private final JpaDocumentRepository repository;

    @Override
    public ProcessedDocument save(ProcessedDocument document) {
        log.info("save document: {}", document.id());

        var entity = DocumentEntity.fromDomain(document);
        var savedEntity = repository.save(entity);

        log.info("Successfully saved document with ID: {}", document.id());
        return savedEntity.toDomain();
    }

    @Override
    public Optional<ProcessedDocument> findById(UUID id) {
        log.info("find document by id: {}", id);

        return repository.findById(id).map(DocumentEntity::toDomain);
    }

    @Override
    public Boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        log.info("delete document by id: {}", id);
        repository.deleteById(id);
    }
}
