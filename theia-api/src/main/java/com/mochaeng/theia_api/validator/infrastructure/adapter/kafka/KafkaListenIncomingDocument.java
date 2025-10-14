package com.mochaeng.theia_api.validator.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.validator.application.service.VerifyDocumentService;
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
public class KafkaListenIncomingDocument {

    private final VerifyDocumentService verifier;

    @KafkaListener(
        topics = "${kafka.topics.document-uploaded}",
        groupId = "${spring.kafka.consumer.group-id}-validator",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void receiveUploadedDocument(
        @Payload DocumentMessage message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info(
            "received incoming-document event for document '{}' from topic '{}'",
            message.documentID(),
            topic
        );

        var verification = verifier.verify(message);
        if (!verification.isEmpty()) {
            log.info(
                "failed to validate incoming-document '{}': {}",
                message.documentID(),
                verification.get().message()
            );
        }
    }
}
