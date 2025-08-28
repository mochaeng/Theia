package com.mochaeng.theia_api.shared.config.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.pdf-uploaded}")
    private String documentUploadedTopic;

    @Value("${kafka.topics.pdf-processed}")
    private String documentProcessedTopic;

    @Value("${kafka.topics.pdf-failed}")
    private String documentFailedTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers
        );
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic documentUploadedTopic() {
        return new NewTopic(documentUploadedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic documentProcessedTopic() {
        return new NewTopic(documentProcessedTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic documentFailedTopic() {
        return new NewTopic(documentFailedTopic, 3, (short) 1);
    }
}
