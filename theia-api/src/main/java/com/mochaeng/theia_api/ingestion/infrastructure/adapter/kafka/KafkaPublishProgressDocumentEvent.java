package com.mochaeng.theia_api.ingestion.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.notification.domain.DocumentProgressEvent;
import com.mochaeng.theia_api.processing.application.port.out.PublishProgressDocumentEventPort;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaPublishProgressDocumentEvent
    implements PublishProgressDocumentEventPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String progressTopic;

    public KafkaPublishProgressDocumentEvent(
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${kafka.topics.document-progress}") String documentUploadedTopic
    ) {
        this.kafkaTemplate = new KafkaTemplate<>(
            kafkaTemplate.getProducerFactory()
        );
        this.progressTopic = documentUploadedTopic;
    }

    @Override
    public void publish(DocumentProgressEvent event) {
        log.info(
            "publishing event [{}] to topic [{}] with key [{}]",
            event,
            progressTopic,
            event.documentID()
        );

        var key = event.documentID().toString();

        kafkaTemplate
            .send(progressTopic, key, event)
            .orTimeout(5, TimeUnit.SECONDS)
            .whenComplete(
                ((result, ex) -> {
                        if (ex == null) {
                            log.info(
                                "event published successfully to topic [{}] with key [{}] at offset {}",
                                progressTopic,
                                key,
                                result.getRecordMetadata().offset()
                            );
                        } else {
                            log.error(
                                "failed  to publish event to topic [{}] with key [{}]",
                                progressTopic,
                                key,
                                ex
                            );
                        }
                    })
            );
    }
}
