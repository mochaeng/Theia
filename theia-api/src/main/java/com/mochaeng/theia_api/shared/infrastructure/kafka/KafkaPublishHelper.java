package com.mochaeng.theia_api.shared.infrastructure.kafka;

import io.vavr.control.Try;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaPublishHelper {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.async-timeout-seconds}")
    private long asyncTimeoutSeconds;

    @Value("${kafka.sync-timeout-seconds}")
    private long syncTimeoutSeconds;

    public Try<Void> publishAsync(String topic, String key, Object message) {
        return publishAsync(topic, key, message, asyncTimeoutSeconds);
    }

    public Try<Void> publishAsync(
        String topic,
        String key,
        Object message,
        long timeoutSeconds
    ) {
        log.info("async-publishing to topic '{}' with key '{}'", topic, key);

        return Try.run(() -> {
            kafkaTemplate
                .send(topic, key, message)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                            "published successfully to [{}] key [{}] offset {}",
                            topic,
                            key,
                            result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error(
                            "failed to async-publish to [{}] key [{}]",
                            topic,
                            key,
                            ex
                        );
                    }
                });
        });
    }

    public Try<SendResult<String, Object>> publishSync(
        String topic,
        String key,
        Object message
    ) {
        return publishSync(topic, key, message, syncTimeoutSeconds);
    }

    private Try<SendResult<String, Object>> publishSync(
        String topic,
        String key,
        Object message,
        long syncTimeoutSeconds
    ) {
        log.info("sync-publishing to '{}' key '{}'", topic, key);

        return Try.of(() -> {
            var future = kafkaTemplate
                .send(topic, key, message)
                .orTimeout(syncTimeoutSeconds, TimeUnit.SECONDS);

            var result = future.get(syncTimeoutSeconds, TimeUnit.SECONDS);

            log.info(
                "published synchronously to [{}] key [{}] offset {}",
                topic,
                key,
                result.getRecordMetadata().offset()
            );

            return result;
        });
    }
}
