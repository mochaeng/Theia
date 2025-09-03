package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OllamaRequest {

    private String model;
    private String input;

    @JsonProperty("keep_alive")
    private String keepAlive;

    public OllamaRequest(String model, String input, String keepAlive) {
        this.model = model;
        this.input = input;
        this.keepAlive = keepAlive;
    }
}
