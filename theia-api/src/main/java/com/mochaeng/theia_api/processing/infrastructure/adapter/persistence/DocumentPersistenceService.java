package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.application.dto.PersistenceDocumentResult;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.DocumentEntity;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.JpaDocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentPersistenceService {

    private final JpaDocumentRepository documentRepository;
    private final AuthorPersistenceService authorService;
    private final FieldPersistenceService fieldService;

    @Transactional
    public PersistenceDocumentResult persistDocument(
        ProcessedDocument document
    ) {
        try {
            log.info("persisting document with id [{}]", document.id());

            var documentEntity = findOrCreateDocumentEntity(document);
            var authorsEntities = authorService.processAuthors(
                document.authors()
            );

            documentEntity.setAuthors(authorsEntities);
            var savedDocument = documentRepository.save(documentEntity);

            fieldService.persistFieldEmbeddings(
                document.fieldEmbeddings(),
                savedDocument
            );

            log.info("document with id [{}] successfully saved", document.id());

            return PersistenceDocumentResult.success();
        } catch (Exception e) {
            log.error(
                "failed to persist document with id [{}]: {}",
                document.id(),
                e.getMessage()
            );
            return PersistenceDocumentResult.failure(
                "PERSISTENCE_ERROR",
                e.getMessage()
            );
        }
    }

    public boolean existsByHash(byte[] hash) {
        return documentRepository.existsByFileHash(hash);
    }

    private DocumentEntity findOrCreateDocumentEntity(
        ProcessedDocument document
    ) {
        return documentRepository
            .findById(document.id())
            .orElseGet(() -> DocumentEntity.fromDomain(document));
    }
}
