package com.mochaeng.theia_api.shared.infrastructure.ollama;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
            .build();
    }

    @Bean("ollamaRetry")
    public Retry ollamaRetry() {
        return Retry.of(
            "ollama-retry",
            RetryConfig.custom()
                .maxAttempts(props.getMaxRetries())
                .waitDuration(props.getRetryDelay())
                .retryOnException(
                    ex ->
                        ex instanceof ResourceAccessException ||
                        ex instanceof SocketTimeoutException
                )
                .build()
        );
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) props.getReadTimeout().toMillis());
        return factory;
    }
}
