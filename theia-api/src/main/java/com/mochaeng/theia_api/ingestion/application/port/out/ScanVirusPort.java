package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.ingestion.domain.model.Document;

public interface ScanVirusPort {
    boolean hasVirus(Document document);
}
