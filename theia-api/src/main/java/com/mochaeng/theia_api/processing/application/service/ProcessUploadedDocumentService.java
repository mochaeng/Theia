package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.application.dto.DocumentDownloadResult;
import com.mochaeng.theia_api.processing.application.port.in.ProcessUploadedDocumentUseCase;
import com.mochaeng.theia_api.processing.application.port.out.DownloadUploadedDocumentPort;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessUploadedDocumentService
    implements ProcessUploadedDocumentUseCase {

    private final DownloadUploadedDocumentPort downloadUploadedDocument;

    //    private final ExtractDocumentDataPort extractDocumentData;

    @Override
    public void process(DocumentUploadedMessage message) {
        log.info("Processing uploaded document event");

        DocumentDownloadResult result = downloadUploadedDocument.download(
            message
        );
        if (!result.isSuccess()) {
            // publish failed downloand event to kafka
        }
    }
}
