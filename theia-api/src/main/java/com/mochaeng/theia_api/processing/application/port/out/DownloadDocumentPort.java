package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.application.dto.DownloadDocumentResult;
import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;

public interface DownloadDocumentPort {
    DownloadDocumentResult download(IncomingDocumentMessage message);
}
