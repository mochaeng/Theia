package com.mochaeng.theia_api.ingestion.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.ingestion.application.port.out.PublishIncomingDocumentPort;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaPublishIncomingDocumentEvent
    implements PublishIncomingDocumentPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String incomingDocumentTopic;
    private final long asyncTimeoutSeconds;
    private final long syncTimeoutSeconds;

    @Autowired
    public KafkaPublishIncomingDocumentEvent(
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value(
            "${kafka.topics.document-uploaded}"
        ) String documentUploadedTopic,
        @Value("${kafka.timeout.async:10}") long asyncTimeoutSeconds,
        @Value("${kafka.timeout.sync:2}") long syncTimeoutSeconds
    ) {
        this.kafkaTemplate = new KafkaTemplate<>(
            kafkaTemplate.getProducerFactory()
        );
        this.incomingDocumentTopic = documentUploadedTopic;
        this.asyncTimeoutSeconds = asyncTimeoutSeconds;
        this.syncTimeoutSeconds = syncTimeoutSeconds;
    }

    @Override
    public Either<PublishUploadedDocumentError, Void> publishAsync(
        DocumentMessage message
    ) {
        var key = message.documentID().toString();

        log.info(
            "async-publishing event {} to topic [{}] with key [{}]",
            message,
            incomingDocumentTopic,
            key
        );

        return Try.run(() ->
            kafkaTemplate
                .send(incomingDocumentTopic, key, message)
                .orTimeout(asyncTimeoutSeconds, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                            "event published successfully to topic [{}] with key [{}] at offset {}",
                            incomingDocumentTopic,
                            key,
                            result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error(
                            "failed to async-publish event to topic [{}] with key [{}]",
                            incomingDocumentTopic,
                            key,
                            ex
                        );
                    }
                })
        )
            .toEither()
            .mapLeft(ex -> new PublishUploadedDocumentError(ex.getMessage()));
    }

    @Override
    public Either<PublishUploadedDocumentError, Void> publishSync(
        DocumentMessage message
    ) {
        var key = message.documentID().toString();

        log.info(
            "publishing event {} to topic [{}] with key [{}]",
            message,
            incomingDocumentTopic,
            key
        );

        return Try.run(() -> {
            var future = kafkaTemplate
                .send(incomingDocumentTopic, key, message)
                .orTimeout(syncTimeoutSeconds, TimeUnit.SECONDS);

            var result = future.get(syncTimeoutSeconds, TimeUnit.SECONDS);

            log.info(
                "event published synchronously to topic [{}] with key [{}] at offset {}",
                incomingDocumentTopic,
                key,
                result.getRecordMetadata().offset()
            );
        })
            .toEither()
            .mapLeft(ex -> new PublishUploadedDocumentError(ex.getMessage()));
    }
}
