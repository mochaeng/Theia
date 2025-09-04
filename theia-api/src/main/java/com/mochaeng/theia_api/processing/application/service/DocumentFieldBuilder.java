package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.domain.model.DocumentField;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DocumentFieldBuilder {

    public Map<DocumentField, String> buildFieldTexts(
        DocumentMetadata metadata
    ) {
        var fieldTexts = new HashMap<DocumentField, String>();

        if (hasContent(metadata.title())) {
            fieldTexts.put(DocumentField.TITLE, metadata.title());
        }
        if (hasContent(metadata.abstractText())) {
            fieldTexts.put(DocumentField.ABSTRACT, metadata.abstractText());
        }

        return fieldTexts;
    }

    private boolean hasContent(String content) {
        return content != null && !content.trim().isEmpty();
    }
}
