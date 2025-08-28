package com.mochaeng.theia_api;

import com.mochaeng.theia_api.ingestion.application.port.out.PublishUploadedDocumentEventPort;
import com.mochaeng.theia_api.shared.config.kafka.KafkaConfig;
import com.mochaeng.theia_api.shared.config.kafka.KafkaTopicConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TheiaApiApplicationTests {

    //    @MockitoBean
    //    private PublishUploadedDocumentEventPort kafkaEventPublisher;
    //
    //    @MockitoBean
    //    private KafkaConfig kafkaConfig;
    //
    //    @MockitoBean
    //    private KafkaTopicConfig kafkaTopicConfig;
    //
    //    @MockitoBean
    //    private KafkaListenerContainerFactory kafkaListenerContainerFactory;

    @Test
    void contextLoads() {}
}
