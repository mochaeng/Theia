package com.mochaeng.theia_api.query.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.query.application.port.out.GenerateQueryEmbeddingPort;
import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import com.mochaeng.theia_api.shared.infrastructure.ollama.OllamaHelpers;
import com.mochaeng.theia_api.shared.infrastructure.ollama.dto.OllamaResponse;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaGenerateQueryEmbedding
    implements GenerateQueryEmbeddingPort {

    private final OllamaHelpers ollamaHelpers;

    @Override
    public Either<EmbeddingError, float[]> generate(String text) {
        log.info("generating embeddings for given text: {}", text);

        return ollamaHelpers
            .makeOllamaCall(text)
            .map(OllamaResponse::getFirstEmbedding)
            .peek(embedding -> log.info("embedding: {}", embedding));
    }
}
