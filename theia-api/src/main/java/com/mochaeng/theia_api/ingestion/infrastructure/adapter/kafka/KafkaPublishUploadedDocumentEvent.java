package com.mochaeng.theia_api.ingestion.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.ingestion.application.port.out.PublishUploadedDocumentPort;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaPublishUploadedDocumentEvent
    implements PublishUploadedDocumentPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String documentUploadedTopic;

    @Autowired
    public KafkaPublishUploadedDocumentEvent(
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${kafka.topics.document-uploaded}") String documentUploadedTopic
    ) {
        this.kafkaTemplate = new KafkaTemplate<>(
            kafkaTemplate.getProducerFactory()
        );
        this.documentUploadedTopic = documentUploadedTopic;
    }

    @Override
    public void publish(DocumentUploadedMessage event) {
        publishEvent(
            documentUploadedTopic,
            event.documentID().toString(),
            event
        );
    }

    private void publishEvent(String topic, String key, Object event) {
        log.debug(
            "publishing event {} to topic [{}] with key [{}]",
            event,
            topic,
            key
        );

        kafkaTemplate
            .send(topic, key, event)
            .orTimeout(30, TimeUnit.SECONDS)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                        "event published successfully to topic [{}] with key [{}] at offset {}",
                        topic,
                        key,
                        result.getRecordMetadata().offset()
                    );
                } else {
                    log.error(
                        "failed  to publish event to topic [{}] with key [{}]",
                        topic,
                        key,
                        ex
                    );
                }
            });
    }
}
