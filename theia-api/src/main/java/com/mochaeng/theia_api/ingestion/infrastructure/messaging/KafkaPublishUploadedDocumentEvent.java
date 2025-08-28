package com.mochaeng.theia_api.ingestion.infrastructure.messaging;

import com.mochaeng.theia_api.ingestion.application.port.out.PublishUploadedDocumentEventPort;
import com.mochaeng.theia_api.shared.application.events.DocumentUploadedEvent;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("kafkaPublishUploadedDocument")
@Slf4j
public class KafkaPublishUploadedDocumentEvent
    implements PublishUploadedDocumentEventPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String documentUploadedTopic;

    @Autowired
    public KafkaPublishUploadedDocumentEvent(
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${kafka.topics.pdf-uploaded}") String documentUploadedTopic
    ) {
        this.kafkaTemplate = new KafkaTemplate<>(
            kafkaTemplate.getProducerFactory()
        );
        this.documentUploadedTopic = documentUploadedTopic;
    }

    @Override
    public void publish(DocumentUploadedEvent event) {
        publishEvent(
            documentUploadedTopic,
            event.documentID().toString(),
            event
        );
    }

    private void publishEvent(String topic, String key, Object event) {
        log.debug(
            "Publishing event {} to topic [{}] with key [{}]",
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
                        "Event published successfully to topic [{}] with key [{}] at offset {}",
                        topic,
                        key,
                        result.getRecordMetadata().offset()
                    );
                } else {
                    log.error(
                        "Failed  to publish event to topic [{}] with key [{}]",
                        topic,
                        key,
                        ex
                    );
                }
            });
    }
}
