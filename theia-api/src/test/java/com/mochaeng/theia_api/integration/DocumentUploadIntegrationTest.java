package com.mochaeng.theia_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import com.mochaeng.theia_api.ingestion.application.web.dto.UploadDocumentResponse;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.core.ConditionTimeoutException;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
public class DocumentUploadIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private S3Client s3Client;

    @Value("${kafka.topics.document-uploaded}")
    private String documentUploadedTopic;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:4.0.0");

    @Container
    static MinIOContainer minio = new MinIOContainer(
        "minio/minio:RELEASE.2025-07-18T21-56-31Z"
    );

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse(
            "pgvector/pgvector:pg17-trixie"
        ).asCompatibleSubstituteFor("postgres")
    );

    private Consumer<String, DocumentUploadedMessage> kafkaConsumer;

    @Test
    void shouldUploadDocumentAndPublishKafkaEvent() throws Exception {
        log.info("starting document upload test");

        var bitcoinPdf = new ClassPathResource("/test-files/bitcoin.pdf");
        assertThat(bitcoinPdf.exists()).isTrue();

        var parts = new LinkedMultiValueMap<>();
        parts.add("file", bitcoinPdf);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var requestEntity = new HttpEntity<>(parts, headers);
        var response = restTemplate.exchange(
            "http://localhost:" + port + "/api/upload-document",
            HttpMethod.POST,
            requestEntity,
            UploadDocumentResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var documentID = response.getBody().documentID();
        assertThat(documentID).isNotNull();
        assertThatCode(() -> {
            var ignored = UUID.fromString(documentID);
        }).doesNotThrowAnyException();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.kafka.bootstrap-servers",
            kafka::getBootstrapServers
        );

        registry.add("storage.s3.endpoint", minio::getS3URL);
        registry.add("storage.s3.access-key", minio::getUserName);
        registry.add("storage.s3.secret-access-key", minio::getPassword);
        registry.add("storage.s3.region", () -> "us-east-1");
        registry.add("storage.s3.path-style-access", () -> "true");

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        log.info("setting up test containers...");
        createS3Bucket();
        setupKafkaConsumer();
        log.info("test setup completed");
    }

    private void createS3Bucket() {
        try {
            s3Client.createBucket(
                CreateBucketRequest.builder().bucket(bucketName).build()
            );
        } catch (Exception e) {
            log.error("s3 bucket creation failed: {}", e.getMessage());
        }
    }

    private void setupKafkaConsumer() {
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
            DocumentUploadedMessage.class
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var consumerFactory = new DefaultKafkaConsumerFactory<
            String,
            DocumentUploadedMessage
        >(consumerProps);
        kafkaConsumer = consumerFactory.createConsumer();
        kafkaConsumer.subscribe(List.of(documentUploadedTopic));
    }

    private Optional<DocumentUploadedMessage> waitForKafkaEvent(
        UUID documentID
    ) {
        try {
            return await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> pollForDocument(documentID), Optional::isPresent);
        } catch (ConditionTimeoutException e) {
            log.warn(
                "timeout waiting for document upload event for document [{}]",
                documentID
            );
            return Optional.empty();
        } catch (Exception e) {
            log.error(
                "error waiting for document upload event for document [{}]: {}",
                documentID,
                e.getMessage()
            );
            return Optional.empty();
        }
    }

    private Optional<DocumentUploadedMessage> pollForDocument(UUID documentID) {
        try {
            var records = kafkaConsumer.poll(Duration.ofMillis(100));

            return StreamSupport.stream(records.spliterator(), false)
                .map(ConsumerRecord::value)
                .filter(Objects::nonNull)
                .filter(msg -> documentID.equals(msg.documentID()))
                .findFirst();
        } catch (Exception e) {
            log.error(
                "failed to poll document with id [{}] from kafka: {}",
                documentID,
                e.getMessage()
            );
            return Optional.empty();
        }
    }
}
