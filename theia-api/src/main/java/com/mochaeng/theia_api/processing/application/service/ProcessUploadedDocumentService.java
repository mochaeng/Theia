package com.mochaeng.theia_api.processing.application.service;

import com.mochaeng.theia_api.processing.application.port.in.ProcessUploadedDocumentUseCase;
import com.mochaeng.theia_api.shared.application.events.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessUploadedDocumentService
    implements ProcessUploadedDocumentUseCase {

    @Override
    public void process(DocumentUploadedEvent event) {}
}
