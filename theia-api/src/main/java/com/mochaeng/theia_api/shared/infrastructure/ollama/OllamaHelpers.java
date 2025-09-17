package com.mochaeng.theia_api.shared.infrastructure.ollama;

import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import com.mochaeng.theia_api.shared.infrastructure.ollama.dto.OllamaRequest;
import com.mochaeng.theia_api.shared.infrastructure.ollama.dto.OllamaResponse;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaHelpers {

    @Qualifier("ollamaRestClient")
    private final RestClient restClient;

    @Qualifier("ollamaRetry")
    private final Retry retry;

    private final OllamaProperties props;

    public Either<EmbeddingError, OllamaResponse> makeOllamaCall(String text) {
        return validateInput(text).flatMap(validText -> {
                var supplier = Retry.decorateSupplier(retry, () ->
                    performHttpCall(validText)
                );
                return Try.ofSupplier(supplier)
                    .toEither()
                    .mapLeft(this::mapThrowableToError);
            });
    }

    private OllamaResponse performHttpCall(String text) {
        log.info("making http call to ollama");

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
            throw new RuntimeException("Received null response from ollama");
        }

        if (!response.hasEmbeddings()) {
            throw new RuntimeException(
                "Ollama response contains no embeddings array"
            );
        }

        if (
            response.getFirstEmbedding() == null ||
            response.getFirstEmbedding().length == 0
        ) {
            throw new RuntimeException(
                "Ollama response contains empty embeddings array"
            );
        }

        return response;
    }

    private EmbeddingError mapThrowableToError(Throwable throwable) {
        return switch (throwable) {
            case ResourceAccessException e -> mapResourceAccessException(e);
            case RuntimeException e -> new EmbeddingError.InvalidInput(
                "Unexpected error during ollama call: %s".formatted(
                    e.getMessage()
                )
            );
            case null, default -> new EmbeddingError.UnknownError(
                "Unexpected error occurred while calling ollama"
            );
        };
    }

    private EmbeddingError mapResourceAccessException(
        ResourceAccessException exception
    ) {
        var cause = exception.getCause();

        return switch (cause) {
            case SocketTimeoutException e -> new EmbeddingError.UnavailableService(
                "Ollama request timed out: %s".formatted(e.getMessage())
            );
            case ConnectException e -> new EmbeddingError.UnavailableService(
                "Ollama service is unreachable: %s".formatted(e.getMessage())
            );
            case UnknownHostException e -> new EmbeddingError.UnavailableService(
                "Cannot resolve ollama host: %s".formatted(e.getMessage())
            );
            case null, default -> new EmbeddingError.UnknownError(
                "Network connectivity issue"
            );
        };
    }

    private Either<EmbeddingError, String> validateInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Either.left(
                new EmbeddingError.InvalidInput(
                    "Input text cannot be null or empty"
                )
            );
        }
        return Either.right(text.trim());
    }
}
