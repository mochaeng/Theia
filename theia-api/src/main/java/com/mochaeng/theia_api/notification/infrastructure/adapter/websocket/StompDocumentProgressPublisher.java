package com.mochaeng.theia_api.notification.infrastructure.adapter.websocket;

import com.mochaeng.theia_api.notification.application.port.out.NotifyDocumentProgressPort;
import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.notification.infrastructure.util.TopicPath;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompDocumentProgressPublisher
    implements NotifyDocumentProgressPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notify(DocumentProgressEvent event) {
        if (event == null || event.documentID() == null) {
            return;
        }

        var destination = TopicPath.documentProgress(event.documentID());

        log.info("sending DocumentProgressEvent: {}", event);

        Try.run(() ->
            messagingTemplate.convertAndSend(destination, event)
        ).onFailure(ex ->
            log.error("failed to send message to '{}'", ex.getMessage())
        );
    }
}
