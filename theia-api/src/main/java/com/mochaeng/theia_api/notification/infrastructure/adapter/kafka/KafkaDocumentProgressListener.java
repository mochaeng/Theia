package com.mochaeng.theia_api.notification.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.notification.application.port.out.NotifyDocumentProgressPort;
import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDocumentProgressListener {

    private final NotifyDocumentProgressPort notifier;

    @KafkaListener(
        topics = "${kafka.topics.document-progress}",
        groupId = "notification-document-progress-ws"
    )
    public void onDocumentProgress(DocumentProgressEvent event) {
        if (event == null) {
            log.warn("received null DocumentProgressEvent");
            return;
        }

        log.info("received DocumentProgressEvent '{}'", event.status());

        notifier.notify(event);
    }
}
