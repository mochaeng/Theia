package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.domain.model.FieldEmbedding;
import com.mochaeng.theia_api.shared.infrastructure.jpa.DocumentEntity;
import com.mochaeng.theia_api.shared.infrastructure.jpa.FieldEntity;
import com.mochaeng.theia_api.shared.infrastructure.jpa.JpaFieldRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FieldPersistenceService {

    private final JpaFieldRepository fieldRepository;

    public void persistFieldEmbeddings(
        List<FieldEmbedding> fieldEmbeddings,
        DocumentEntity documentEntity
    ) {
        var fieldEntities = fieldEmbeddings
            .stream()
            .map(field -> createFieldEntity(field, documentEntity))
            .toList();

        fieldRepository.saveAll(fieldEntities);
    }

    private FieldEntity createFieldEntity(
        FieldEmbedding fieldEmbedding,
        DocumentEntity documentEntity
    ) {
        return FieldEntity.builder()
            .fieldType(fieldEmbedding.fieldName().toString())
            .fieldText(fieldEmbedding.text())
            .embedding(fieldEmbedding.embedding())
            .model(fieldEmbedding.metadata().model())
            .tokenCount(fieldEmbedding.metadata().tokenCount())
            .processingTimeMs(fieldEmbedding.metadata().processingTimeMs())
            .document(documentEntity)
            .build();
    }
}
