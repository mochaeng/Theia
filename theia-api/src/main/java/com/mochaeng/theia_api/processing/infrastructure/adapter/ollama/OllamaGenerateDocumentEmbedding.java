package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.application.dto.DocumentEmbeddings;
import com.mochaeng.theia_api.processing.application.dto.DocumentFieldBuilder;
import com.mochaeng.theia_api.processing.application.dto.EmbeddingDocumentResult;
import com.mochaeng.theia_api.processing.application.dto.FieldEmbedding;
import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingPort;
import com.mochaeng.theia_api.processing.domain.model.DocumentField;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto.OllamaRequest;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto.OllamaResponse;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaInvalidResponse;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaTimeoutException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaUnavailableException;
import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component("ollamaGenerateDocumentEmbedding")
@RequiredArgsConstructor
@Slf4j
public class OllamaGenerateDocumentEmbedding
    implements GenerateDocumentEmbeddingPort {

    @Qualifier("ollamaRestClient")
    private final RestClient restClient;

    @Qualifier("ollamaRetryTemplate")
    private final RetryTemplate retryTemplate;

    private final OllamaProperties props;
    private final DocumentFieldBuilder textBuilder;

    @Override
    public EmbeddingDocumentResult generate(DocumentMetadata metadata) {
        log.info(
            "Generating embeddings for document: {}",
            metadata.documentId()
        );

        try {
            var fieldTexts = textBuilder.buildFieldTexts(metadata);
            var fieldEmbeddings = new ArrayList<FieldEmbedding>();

            for (var entry : fieldTexts.entrySet()) {
                var field = entry.getKey();
                var text = TextNormalizer.forEmbedding(
                    entry.getValue(),
                    props.getMaxTextLength()
                );

                if (text.isEmpty()) {
                    log.debug("Skipping empty text for field: {}", field);
                    continue;
                }

                try {
                    var fieldEmbedding = generateFieldEmbedding(field, text);
                    fieldEmbeddings.add(fieldEmbedding);
                } catch (OllamaException e) {
                    log.warn(
                        "Failed to generate embedding for field {} - {}",
                        field,
                        e.getMessage()
                    );
                }
            }

            var documentEmbeddings = DocumentEmbeddings.builder()
                .documentId(metadata.documentId())
                .fieldEmbeddings(fieldEmbeddings)
                .build();

            log.info(
                "Successfully generated embeddings for {} fields of document: {}",
                fieldEmbeddings.size(),
                metadata.documentId()
            );

            return EmbeddingDocumentResult.success(documentEmbeddings);
        } catch (Exception e) {
            log.error(
                "Error generating embeddings for document: {}",
                metadata.documentId(),
                e
            );
            return EmbeddingDocumentResult.failure(
                "UNEXPECTED_ERROR",
                "Unexpected error: " + e.getMessage()
            );
        }
    }

    private FieldEmbedding generateFieldEmbedding(
        DocumentField field,
        String text
    ) {
        log.info(
            "Generating embedding for field {}, text length: {}",
            field,
            text.length()
        );

        var startingTime = Instant.now();
        try {
            var response = makeOllamaCallWithRetry(text);

            var endTime = Instant.now();
            var totalTime = Duration.between(startingTime, endTime).toMillis();

            var fieldEmbedding = FieldEmbedding.builder()
                .fieldName(field)
                .embedding(response.getFirstEmbedding())
                .text(text)
                .metadata(
                    EmbeddingMetadata.builder()
                        .model(response.model())
                        .tokenCount(response.promptEvalCount())
                        .processingTimeMs(totalTime)
                        .build()
                )
                .build();

            log.info(
                "Successfully embedded field '{}' with {} dimensions in {}ms",
                field,
                fieldEmbedding.dimensions(),
                totalTime
            );

            return fieldEmbedding;
        } catch (OllamaException e) {
            log.warn("Ollama error for field {}:{}", field, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Unexpected error for field {}:{}", field, e.getMessage());
            throw new OllamaException(
                "Unexpected error for field {}" + field,
                "UNEXPECTED_ERROR",
                e
            );
        }
    }

    private OllamaResponse makeOllamaCallWithRetry(String text) {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                log.debug(
                    "Retrying Ollama call (attempt {})",
                    context.getRetryCount() + 1
                );
            }
            return makeOllamaCall(text);
        });
    }

    private OllamaResponse makeOllamaCall(String text) {
        log.info("Making HTTP Call to Ollama");

        try {
            var request = new OllamaRequest(
                props.getModel(),
                text,
                props.getKeepAlive()
            );

            var response = restClient
                .post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);

            if (response == null) {
                throw new OllamaInvalidResponse("Null response from Ollama");
            }

            if (!response.hasEmbeddings()) {
                throw new OllamaInvalidResponse(
                    "Response contains no embeddings"
                );
            }

            if (
                response.getFirstEmbedding() == null ||
                response.getFirstEmbedding().length == 0
            ) {
                throw new OllamaInvalidResponse(
                    "Empty embeddings array in response"
                );
            }

            return response;
        } catch (ResourceAccessException e) {
            log.debug(
                "ResourceAccessException for Ollama details - message: {}, cause: {}, cause type: {}",
                e.getMessage(),
                e.getCause(),
                e.getCause() != null
                    ? e.getCause().getClass().getName()
                    : "null"
            );

            Throwable cause = e.getCause();

            if (cause instanceof SocketTimeoutException) {
                throw new OllamaTimeoutException(
                    "Timeout while calling Ollama",
                    cause
                );
            }

            if (
                cause instanceof java.net.ConnectException ||
                cause instanceof java.net.UnknownHostException
            ) {
                throw new OllamaUnavailableException(
                    "Ollama service is unreachable",
                    cause
                );
            }

            throw new OllamaException(
                "Network error calling Ollama",
                "NETWORK_ERROR",
                e
            );
        }
    }
}
