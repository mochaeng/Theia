package com.mochaeng.theia_api.shared.infrastructure.ollama;

import com.mochaeng.theia_api.shared.infrastructure.ollama.dto.OllamaRequest;
import com.mochaeng.theia_api.shared.infrastructure.ollama.dto.OllamaResponse;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.net.SocketTimeoutException;
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

    public Either<OllamaError, OllamaResponse> makeOllamaCall(String text) {
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
            throw new RuntimeException("Null response from ollama");
        }

        if (!response.hasEmbeddings()) {
            throw new RuntimeException("Response contains no embeddings");
        }

        if (
            response.getFirstEmbedding() == null ||
            response.getFirstEmbedding().length == 0
        ) {
            throw new RuntimeException("Empty embeddings array in response");
        }

        return response;
    }

    private OllamaError mapThrowableToError(Throwable throwable) {
        return switch (throwable) {
            case ResourceAccessException ex -> mapResourceAccessException(ex);
            case SocketTimeoutException ex -> new OllamaError.Timeout(
                "Socket timeout occurred",
                ex.getMessage()
            );
            case RuntimeException ex when (
                ex.getMessage() != null && ex.getMessage().contains("timeout")
            ) -> new OllamaError.Timeout("Request timeout", ex.getMessage());
            default -> new OllamaError.UnknownError(
                "Unexpected error occurred",
                throwable.getClass().getName() + ": " + throwable.getMessage()
            );
        };
    }

    private OllamaError mapResourceAccessException(
        ResourceAccessException exception
    ) {
        var cause = exception.getCause();

        return switch (cause) {
            case SocketTimeoutException e -> new OllamaError.Timeout(
                "Timeout while calling Ollama",
                "Socket timeout: " + e.getMessage()
            );
            case java.net.ConnectException e -> new OllamaError.Unavailable(
                "Ollama service is unreachable",
                "Connection refused: " + e.getMessage()
            );
            case java.net.UnknownHostException e -> new OllamaError.Unavailable(
                "Ollama host is unknown",
                "Unknown host: " + e.getMessage()
            );
            case null, default -> new OllamaError.NetworkError(
                "Network error calling Ollama",
                exception.getMessage()
            );
        };
    }

    private Either<OllamaError, String> validateInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Either.left(
                new OllamaError.InvalidInput(
                    "Input text cannot be null or empty",
                    "Received"
                )
            );
        }
        return Either.right(text.trim());
    }
}
