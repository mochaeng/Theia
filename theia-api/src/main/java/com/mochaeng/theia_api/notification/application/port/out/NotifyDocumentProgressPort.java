package com.mochaeng.theia_api.notification.application.port.out;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;

public interface NotifyDocumentProgressPort {
    void notify(DocumentProgressEvent event);
}
