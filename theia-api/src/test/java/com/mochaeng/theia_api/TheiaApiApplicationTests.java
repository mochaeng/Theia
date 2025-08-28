package com.mochaeng.theia_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
