package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaClientException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaServerException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception.OllamaTimeoutException;
import com.mochaeng.theia_api.shared.config.helpers.SharedConfigHelpers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class OllamaConfig {

    private final OllamaProperties props;

    @Bean("ollamaRestClient")
    public RestClient ollamaRestClient() {
        return RestClient.builder()
            .baseUrl(props.getBaseUrl())
            .requestFactory(createRequestFactory())
            .defaultStatusHandler(
                HttpStatusCode::is5xxServerError,
                (request, response) -> {
                    throw new OllamaServerException(
                        "Ollama server error: " + response.getStatusCode()
                    );
                }
            )
            .defaultStatusHandler(
                HttpStatusCode::is4xxClientError,
                (request, response) -> {
                    var body = new String(
                        response.getBody().readAllBytes(),
                        StandardCharsets.UTF_8
                    );
                    throw new OllamaClientException(
                        "Ollama client error: %s - %s".formatted(
                            response.getStatusCode(),
                            body
                        )
                    );
                }
            )
            .build();
    }

    @Bean("ollamaRetryTemplate")
    public RetryTemplate ollamaEmbeddingRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        RetryPolicy retryPolicy = new SimpleRetryPolicy(
            props.getMaxRetries(),
            Map.of(
                ResourceAccessException.class,
                true,
                OllamaServerException.class,
                true,
                OllamaTimeoutException.class,
                true
            )
        );

        return SharedConfigHelpers.getRetryTemplate(
            retryTemplate,
            retryPolicy,
            props.getRetryDelay(),
            props.getRetryMultiplier()
        );
    }

    public ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) props.getReadTimeout().toMillis());
        return factory;
    }
}
