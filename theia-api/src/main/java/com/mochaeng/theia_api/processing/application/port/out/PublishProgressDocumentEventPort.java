package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;

public interface PublishProgressDocumentEventPort {
    void publish(DocumentProgressEvent event);
}
