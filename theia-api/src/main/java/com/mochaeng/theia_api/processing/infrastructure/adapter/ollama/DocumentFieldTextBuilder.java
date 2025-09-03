package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama;

import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DocumentFieldTextBuilder {

    public Map<String, String> buildFieldTexts(DocumentMetadata metadata) {
        Map<String, String> fieldTexts = new HashMap<>();

        if (hasContent(metadata.title())) {
            fieldTexts.put("title", metadata.title());
        }
        if (hasContent(metadata.abstractText())) {
            fieldTexts.put("abstractText", metadata.abstractText());
        }

        return fieldTexts;
    }

    private boolean hasContent(String content) {
        return content != null && !content.trim().isEmpty();
    }
}
