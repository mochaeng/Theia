package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.DownloadDocumentResult;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;

public interface DownloadDocumentPort {
    DownloadDocumentResult download(DocumentUploadedMessage message);
}
