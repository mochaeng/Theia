package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "parser.grobid")
public record GrobidProperties(
    String baseUrl,
    ConsolidateHeaderMode consolidateHeader,
    Duration connectTimeout,
    Duration readTimeout,
    int maxRetries,
    Duration retryDelay,
    float retryMultiplier
) {
    @Getter
    public enum ConsolidateHeaderMode {
        NONE("0"),
        FULL("1"),
        DOI_ONLY("2"),
        EXTRACTED_DOI("3");

        private final String value;

        ConsolidateHeaderMode(String value) {
            this.value = value;
        }
    }
}
