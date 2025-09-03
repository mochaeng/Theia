package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.application.dto.DocumentEmbedding;
import com.mochaeng.theia_api.processing.application.dto.EmbeddingDocumentResult;
import com.mochaeng.theia_api.processing.application.dto.FieldEmbedding;
import com.mochaeng.theia_api.processing.application.port.out.GenerateDocumentEmbeddingPort;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto.OllamaRequest;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto.OllamaResponse;
import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
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
    private final DocumentFieldTextBuilder textBuilder;

    @Override
    public EmbeddingDocumentResult generate(DocumentMetadata metadata) {
        log.info(
            "Generating embeddings per field for document: {}",
            metadata.documentId()
        );

        var fieldTexts = textBuilder.buildFieldTexts(metadata);
        var fieldEmbeddings = new ArrayList<FieldEmbedding>();

        for (Map.Entry<String, String> entry : fieldTexts.entrySet()) {
            String field = entry.getKey();
            String text = TextNormalizer.forEmbedding(
                entry.getValue(),
                props.getMaxTextLength()
            );

            log.info("Embedding field: {}, text: {}", field, text);

            var response = makeOllamaCallWithRetry(text);

            var fieldEmbedding = FieldEmbedding.builder()
                .fieldName(field)
                .embedding(response.getFirstEmbedding())
                .text(text)
                .metadata(
                    EmbeddingMetadata.builder()
                        .model(
                            response.model() != null
                                ? response.model()
                                : props.getModel()
                        )
                        .build()
                )
                .build();

            fieldEmbeddings.add(fieldEmbedding);
            log.info(
                "Embedded  field '{}' with dimensions '{}'",
                field,
                fieldEmbedding.dimensions()
            );
            log.info(Arrays.toString(fieldEmbedding.embedding()));
        }

        var documentEmbeddings = DocumentEmbedding.builder()
            .documentId(metadata.documentId())
            .fieldEmbeddings(fieldEmbeddings)
            .build();

        log.info("Embeddings for document: {}", documentEmbeddings);

        return EmbeddingDocumentResult.success(documentEmbeddings);
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

        var request = new OllamaRequest(props.getModel(), text, "5m");

        var response = restClient
            .post()
            .uri("/api/embed")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(OllamaResponse.class);

        //        if (response == null || !response.hasEmbeddings()) {
        //            throw new OllamaInvalidResponse("Response contains no embeddings");
        //        }

        return response;
    }
}
