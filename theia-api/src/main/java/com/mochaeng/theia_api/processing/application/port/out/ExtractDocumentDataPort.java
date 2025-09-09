package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.ExtractDocumentResult;
import java.util.UUID;

public interface ExtractDocumentDataPort {
    ExtractDocumentResult extract(UUID documentID, byte[] documentBytes);
}
