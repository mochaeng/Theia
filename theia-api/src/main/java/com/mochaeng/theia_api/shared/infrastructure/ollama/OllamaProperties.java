package com.mochaeng.theia_api.shared.infrastructure.ollama;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "ollama.embedding")
public class OllamaProperties {

    private String baseUrl;
    private String model;
    private String keepAlive;

    private Duration connectTimeout;
    private Duration readTimeout;
    private int maxRetries;
    private Duration retryDelay;
    private float retryMultiplier;

    private int maxTextLength;
    private boolean truncateText;
}
