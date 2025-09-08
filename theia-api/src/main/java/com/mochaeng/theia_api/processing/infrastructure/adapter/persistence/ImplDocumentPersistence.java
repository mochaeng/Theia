package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.application.dto.PersistenceDocumentResult;
import com.mochaeng.theia_api.processing.application.port.out.DocumentPersistencePort;
import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.processing.domain.model.FieldEmbedding;
import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@Transactional
public class ImplDocumentPersistence implements DocumentPersistencePort {

    private final JpaDocumentRepository documentRepository;
    private final JpaAuthorRepository authorRepository;
    private final JpaFieldRepository fieldRepository;

    @Override
    public PersistenceDocumentResult save(ProcessedDocument document) {
        log.info("saving document with id [{}]", document.id());

        try {
            var documentEntity = documentRepository
                .findById(document.id())
                .orElseGet(() -> DocumentEntity.fromDomain(document));

            var authorEntities = processAuthors(document.authors());
            documentEntity.setAuthors(authorEntities);

            processFields(document.fieldEmbeddings(), documentEntity);

            documentRepository.save(documentEntity);

            log.info("document with id [{}] successfully saved", document.id());

            return PersistenceDocumentResult.success();
        } catch (Exception e) {
            log.error(
                "failed to save document with id [{}]: {}",
                document.id(),
                e.getMessage(),
                e
            );
            return PersistenceDocumentResult.failure(
                "SAVE_FAILED",
                "failed to save document: " + e.getMessage()
            );
        }
    }

    private Set<AuthorEntity> processAuthors(List<Author> authors) {
        return authors
            .stream()
            .map(author -> {
                try {
                    return authorRepository
                        .findByEmail(author.email())
                        .orElseGet(() -> createNewAuthor(author));
                } catch (Exception e) {
                    log.error(
                        "failed to process author with email {}: {}",
                        author.email(),
                        e.getMessage()
                    );
                    throw new RuntimeException("Author processing failed", e);
                }
            })
            .collect(Collectors.toSet());
    }

    private AuthorEntity createNewAuthor(Author author) {
        try {
            var newAuthor = AuthorEntity.builder()
                .firstName(author.firstName())
                .lastName(author.lastName())
                .email(author.email())
                .build();
            return authorRepository.save(newAuthor);
        } catch (DataIntegrityViolationException e) {
            log.warn(
                "author with email {} already exists, retrieving",
                author.email()
            );
            return authorRepository
                .findByEmail(author.email())
                .orElseThrow(() ->
                    new RuntimeException(
                        "Failed to retrieve author after conflict",
                        e
                    )
                );
        } catch (Exception e) {
            log.error(
                "failed to create author with email {}: {}",
                author.email(),
                e.getMessage()
            );
            throw new RuntimeException("Author creation failed", e);
        }
    }

    private void processFields(
        List<FieldEmbedding> fieldEmbeddings,
        DocumentEntity documentEntity
    ) {
        fieldEmbeddings.forEach(field -> {
            try {
                var fieldEntity = FieldEntity.builder()
                    .fieldType(field.fieldName().toString())
                    .fieldText(field.text())
                    .embedding(field.embedding())
                    .model(field.metadata().model())
                    .tokenCount(field.metadata().tokenCount())
                    .processingTimeMs(field.metadata().processingTimeMs())
                    .document(documentEntity)
                    .build();
                fieldRepository.save(fieldEntity);
            } catch (Exception e) {
                log.error(
                    "failed to save field {} for document {}: {}",
                    field.fieldName(),
                    documentEntity.getId(),
                    e.getMessage()
                );
                throw new RuntimeException("field processing failed", e);
            }
        });
    }
}
