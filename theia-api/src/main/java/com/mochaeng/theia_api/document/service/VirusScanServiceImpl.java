package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.document.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VirusScanServiceImpl implements VirusScanService {
    @Override
    public boolean hasVirus(Document document) {
        log.debug("Performing virus scan for file: {}", document.filename());
        return containsSuspiciousPatterns(document);
    }

    private boolean containsSuspiciousPatterns(Document document) {
        byte[] content = document.content();

        if (content == null || content.length < 4) {
            return false;
        }

        String contentStr = new String(content, 0, Math.min(1000, content.length));
        return contentStr.contains("javascript") ||
            contentStr.contains("/JavaScript") ||
            contentStr.contains("eval(");
    }
}
