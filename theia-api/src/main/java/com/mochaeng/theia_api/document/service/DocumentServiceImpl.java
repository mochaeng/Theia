package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.document.model.Document;
import com.mochaeng.theia_api.storage.s3.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentValidationService validationService;
    private final StorageService storageService;

    @Override
    public void uploadDocument(Document document) {
        validationService.validateDocument(document);

        storageService.storeDocument(document);
    }
}
