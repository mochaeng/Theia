package com.mochaeng.theia_api.document.events;

import com.mochaeng.theia_api.document.dto.DocumentFailedEvent;
import com.mochaeng.theia_api.document.dto.DocumentProcessedEvent;
import com.mochaeng.theia_api.document.dto.DocumentUploadedEvent;
import com.mochaeng.theia_api.shared.config.kafka.KafkaEventPublisher;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisherImpl implements KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.pdf-uploaded}")
    private String documentUploadedTopic;

    @Value("${kafka.topics.pdf-processed}")
    private String documentProcessedTopic;

    @Value("${kafka.topics.pdf-failed}")
    private String documentFailedTopic;

    @Override
    public void publishDocumentUploadedEvent(DocumentUploadedEvent event) {
        publishEvent(
            documentUploadedTopic,
            event.documentID().toString(),
            event
        );
    }

    @Override
    public void publishDocumentProcessedEvent(DocumentProcessedEvent event) {
        publishEvent(
            documentProcessedTopic,
            event.documentID().toString(),
            event
        );
    }

    @Override
    public void publishDocumentFailedEvent(DocumentFailedEvent event) {
        publishEvent(documentFailedTopic, event.documentID().toString(), event);
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            log.debug(
                "Publishing event {} to topic [{}] with key [{}]",
                event,
                topic,
                key
            );

            CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
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
        } catch (Exception e) {
            log.error(
                "Error publishing event to topic [{}] with key [{}]",
                topic,
                key,
                e
            );
            throw new RuntimeException("Failed to publish kafka event", e);
        }
    }
}
