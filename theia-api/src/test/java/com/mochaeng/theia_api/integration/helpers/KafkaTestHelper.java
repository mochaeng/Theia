package com.mochaeng.theia_api.integration.helpers;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.testcontainers.kafka.KafkaContainer;

public class KafkaTestHelper {

    public static Consumer<String, DocumentMessage> createConsumer(
        KafkaContainer kafka,
        String topic
    ) {
        var consumerProps = new HashMap<String, Object>();
        consumerProps.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            kafka.getBootstrapServers()
        );
        consumerProps.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            "test-consumer-group"
        );
        consumerProps.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );
        consumerProps.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            JsonDeserializer.class
        );
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(
            JsonDeserializer.VALUE_DEFAULT_TYPE,
            DocumentMessage.class
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(
            ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG,
            "resolve_canonical_bootstrap_servers_only"
        );

        var consumerFactory = new DefaultKafkaConsumerFactory<
            String,
            DocumentMessage
        >(consumerProps);

        var consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    public static Option<DocumentMessage> pollForDocument(
        Consumer<String, DocumentMessage> consumer,
        UUID documentID
    ) {
        return Try.of(() -> {
            var records = consumer.poll(Duration.ofMillis(100));

            return io.vavr.collection.List.ofAll(records)
                .map(ConsumerRecord::value)
                .filter(Objects::nonNull)
                .find(msg -> documentID.equals(msg.documentID()));
        }).getOrElse(Option.none());
    }
}
