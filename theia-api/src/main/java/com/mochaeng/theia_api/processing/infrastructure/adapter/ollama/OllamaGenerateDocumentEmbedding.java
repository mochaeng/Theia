package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingsPort;
import com.mochaeng.theia_api.processing.application.service.DocumentFieldBuilder;
import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.domain.model.DocumentField;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import com.mochaeng.theia_api.processing.domain.model.FieldEmbedding;
import com.mochaeng.theia_api.shared.application.error.EmbeddingGenerationError;
import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaError;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaHelpers;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaProperties;
import io.vavr.control.Either;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("ollamaGenerateDocumentEmbedding")
@RequiredArgsConstructor
@Slf4j
public class OllamaGenerateDocumentEmbedding
    implements GenerateDocumentEmbeddingsPort {

    private final OllamaHelpers ollamaHelpers;
    private final DocumentFieldBuilder textBuilder;
    private final OllamaProperties props;

    @Override
    public Either<EmbeddingGenerationError, DocumentEmbeddings> generate(
        DocumentMetadata metadata
    ) {
        log.info(
            "generating embeddings for document with id [{}]",
            metadata.documentId()
        );

        var fieldTexts = textBuilder.buildFieldTexts(metadata);
        var fieldEmbeddings = new ArrayList<FieldEmbedding>();

        for (var entry : fieldTexts.entrySet()) {
            var field = entry.getKey();
            var text = TextNormalizer.forEmbedding(
                entry.getValue(),
                props.getMaxTextLength()
            );

            if (text.isEmpty()) {
                log.debug("skipping empty text for field: {}", field);
                continue;
            }

            var fieldResult = generateFieldEmbedding(field, text);
            fieldResult.fold(
                error -> {
                    log.warn(
                        "failed to generate embedding for field {}: {}",
                        field,
                        error
                    );
                    return null;
                },
                fieldEmbedding -> {
                    fieldEmbeddings.add(fieldEmbedding);
                    return fieldEmbedding;
                }
            );
        }

        if (fieldEmbeddings.isEmpty()) {
            return Either.left(
                new EmbeddingGenerationError.UnknownError(
                    "failed to generate any embedding",
                    ""
                )
            );
        }

        var documentEmbeddings = DocumentEmbeddings.builder()
            .documentId(metadata.documentId())
            .fieldEmbeddings(fieldEmbeddings)
            .build();

        log.info(
            "successfully generated embeddings for {} fields for document with id [{}]",
            fieldEmbeddings.size(),
            metadata.documentId()
        );

        return Either.right(documentEmbeddings);
    }

    private Either<
        EmbeddingGenerationError,
        FieldEmbedding
    > generateFieldEmbedding(DocumentField field, String text) {
        log.info("generating embedding for field {}", field);

        var startingTime = Instant.now();
        return ollamaHelpers
            .makeOllamaCall(text)
            .mapLeft(this::mapOllamaError)
            .map(ollamaResponse -> {
                var endTime = Instant.now();
                var totalTime = Duration.between(
                    startingTime,
                    endTime
                ).toMillis();

                var fieldEmbedding = FieldEmbedding.builder()
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

                log.info(
                    "successfully embedded field '{}' with '{}' dimensions in {}ms",
                    field,
                    fieldEmbedding.dimensions(),
                    totalTime
                );

                return fieldEmbedding;
            });
    }

    private EmbeddingGenerationError mapOllamaError(OllamaError ollamaError) {
        return switch (ollamaError) {
            case OllamaError.Timeout(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.ProcessingTimeout(msg, details);
            case OllamaError.Unavailable(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.ServiceUnavailable(msg, details);
            case OllamaError.InvalidInput(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.InvalidInput(msg, details);
            case OllamaError.InvalidResponse(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.InvalidResponse(msg, details);
            case OllamaError.NetworkError(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.ServiceUnavailable(msg, details);
            case OllamaError.UnknownError(
                var msg,
                var details
            ) -> new EmbeddingGenerationError.UnknownError(msg, details);
        };
    }
}
