package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingsPort;
import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentField;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import com.mochaeng.theia_api.processing.domain.model.FieldEmbedding;
import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaHelpers;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaProperties;
import io.vavr.control.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaGenerateDocumentEmbedding
    implements GenerateDocumentEmbeddingsPort {

    private final OllamaHelpers ollamaHelpers;
    private final OllamaProperties props;

    @Override
    public Either<EmbeddingError, DocumentEmbeddings> generate(
        UUID documentID,
        DocumentMetadata metadata
    ) {
        log.info("generating embeddings for '{}'", documentID);

        var fieldTexts = DocumentFieldBuilder.buildFieldTexts(metadata);
        var fieldEmbeddings = new ArrayList<FieldEmbedding>();

        // TODO: sending all requests fields at once to ollama
        for (var entry : fieldTexts.entrySet()) {
            var field = entry.getKey();
            var textEmbedding = TextNormalizer.clean(
                entry.getValue(),
                props.getMaxTextLength()
            );

            if (textEmbedding.isEmpty()) {
                log.debug("skipping empty text for field: {}", field);
                continue;
            }

            generateFieldEmbedding(field, textEmbedding)
                .peek(fieldEmbeddings::add)
                .peekLeft(error ->
                    log.warn(
                        "failed to generate embedding for field {}: {}",
                        field,
                        error
                    )
                );
        }

        if (fieldEmbeddings.isEmpty()) {
            return Either.left(
                new EmbeddingError.UnknownError(
                    "failed to generate any embedding"
                )
            );
        }

        var documentEmbeddings = DocumentEmbeddings.builder()
            //            .documentId(metadata.documentId())
            .documentId(documentID)
            .fieldEmbeddings(fieldEmbeddings)
            .build();

        log.info("successfully generated embeddings for '{}'", documentID);

        return Either.right(documentEmbeddings);
    }

    private Either<EmbeddingError, FieldEmbedding> generateFieldEmbedding(
        DocumentField field,
        String text
    ) {
        log.info("generating embedding for field {}", field);

        var startingTime = System.currentTimeMillis();
        return ollamaHelpers
            .makeOllamaCall(text)
            .map(ollamaResponse -> {
                var totalTime = System.currentTimeMillis() - startingTime;

                return FieldEmbedding.builder()
                    .fieldName(field)
                    .embedding(ollamaResponse.getFirstEmbedding())
                    .text(text)
                    .metadata(
                        EmbeddingMetadata.builder()
                            .model(ollamaResponse.model())
                            .tokenCount(ollamaResponse.promptEvalCount())
                            .processingTimeMs(totalTime)
                            .build()
                    )
                    .build();
            })
            .peek(fieldEmbedding ->
                log.info(
                    "successfully embedded field '{}' with '{}' dimensions in {}ms",
                    field,
                    fieldEmbedding.dimensions(),
                    fieldEmbedding.metadata().processingTimeMs()
                )
            );
    }

    private static class DocumentFieldBuilder {

        public static Map<DocumentField, String> buildFieldTexts(
            DocumentMetadata metadata
        ) {
            var fieldTexts = new HashMap<DocumentField, String>();

            if (hasContent(metadata.title())) {
                fieldTexts.put(DocumentField.TITLE, metadata.title());
            }
            if (hasContent(metadata.abstractText())) {
                fieldTexts.put(DocumentField.ABSTRACT, metadata.abstractText());
            }

            return fieldTexts;
        }

        private static boolean hasContent(String content) {
            return content != null && !content.trim().isEmpty();
        }
    }
}
