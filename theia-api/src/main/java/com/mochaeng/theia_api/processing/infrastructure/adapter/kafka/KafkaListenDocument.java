package com.mochaeng.theia_api.processing.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.processing.application.port.in.ProcessDocumentUseCase;
import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListenDocument {

    private final ProcessDocumentUseCase processDocument;

    @KafkaListener(
        topics = "${kafka.topics.document-validated}",
        groupId = "${spring.kafka.consumer.group-id}-processing",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void receiveUploadedDocument(
        @Payload IncomingDocumentMessage event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info(
            "Received document uploaded event for document ID: {} from topic: {}",
            event.documentID(),
            topic
        );

        try {
            processDocument.process(event);
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage());
        }
    }
}
