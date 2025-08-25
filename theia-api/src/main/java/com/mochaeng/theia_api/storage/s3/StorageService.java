package com.mochaeng.theia_api.storage.s3;

import com.mochaeng.theia_api.document.model.Document;

public interface StorageService {
    String storeDocument(Document document);
}
