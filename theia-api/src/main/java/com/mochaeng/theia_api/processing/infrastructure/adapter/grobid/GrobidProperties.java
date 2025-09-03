package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "parser.grobid")
public class GrobidProperties {

    private String baseUrl = "http://localhost:8070";
    private ConsolidateHeaderMode consolidateHeader =
        ConsolidateHeaderMode.FULL;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(60);
    private int maxRetries = 3;
    private Duration retryDelay = Duration.ofSeconds(2);
    private float retryMultiplier = 2.0f;

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
