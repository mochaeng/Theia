package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidClientException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidServerException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidTimeoutException;
import com.mochaeng.theia_api.shared.infrastructure.helpers.SharedConfigHelpers;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GrobidProperties.class)
public class GrobidConfig {

    private final GrobidProperties props;

    @Bean("grobidRestClient")
    public RestClient getRestClient() {
        return RestClient.builder()
            .baseUrl(props.baseUrl())
            .requestFactory(createRequestFactory())
            .defaultStatusHandler(
                HttpStatusCode::is5xxServerError,
                ((request, response) -> {
                        throw new GrobidServerException(
                            "Server error: " + response.getStatusCode()
                        );
                    })
            )
            .defaultStatusHandler(
                HttpStatusCode::is4xxClientError,
                ((request, response) -> {
                        throw new GrobidClientException(
                            "Client error: " + response.getStatusCode()
                        );
                    })
            )
            .build();
    }

    @Bean("grobidRetryTemplate")
    public RetryTemplate grobidRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        RetryPolicy retryPolicy = new SimpleRetryPolicy(
            props.maxRetries(),
            Map.of(
                GrobidServerException.class,
                true,
                GrobidClientException.class,
                true,
                GrobidTimeoutException.class,
                true
            )
        );

        return SharedConfigHelpers.getRetryTemplate(
            retryTemplate,
            retryPolicy,
            props.retryDelay(),
            props.retryMultiplier()
        );
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.connectTimeout().toMillis());
        factory.setReadTimeout((int) props.readTimeout().toMillis());
        return factory;
    }
}
