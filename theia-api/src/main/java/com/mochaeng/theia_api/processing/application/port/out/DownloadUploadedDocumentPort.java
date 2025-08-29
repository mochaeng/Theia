package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.DocumentDownloadResult;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;

public interface DownloadUploadedDocumentPort {
    DocumentDownloadResult download(DocumentUploadedMessage message);
}
