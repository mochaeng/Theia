package com.mochaeng.theia_api.validator.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import com.mochaeng.theia_api.validator.application.service.VerifyDocumentService;
import io.vavr.control.Try;
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
        @Payload IncomingDocumentMessage message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info(
            "received document uploaded event for document ID '{}' from topic '{}'",
            message.documentID(),
            topic
        );

        Try.run(() -> verifier.verify(message));

        //        var documentBytes = downloadDocument.download("", "");
        //        if (documentBytes.isLeft()) {
        //            log.info("failed to download document");
        //            return;
        //        }

        //        var isValid = documentService.verify(documentBytes.get());
        //        if (isValid.isLeft()) {
        //            log.info("failed to validate document");
        //            return;
        //        }

        // update file bucket location
    }
}
