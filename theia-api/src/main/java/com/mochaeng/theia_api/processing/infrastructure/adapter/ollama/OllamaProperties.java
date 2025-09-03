package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "ollama.embedding")
public class OllamaProperties {

    private String baseUrl = "http://localhost:11434";
    private String model = "nomic-embed-text";

    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofSeconds(2);
    private float retryMultiplier = 2.0f;

    private int maxTextLength = 8000;
    private boolean truncateText = true;
}
