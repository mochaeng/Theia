package com.mochaeng.theia_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.mochaeng.theia_api.ingestion.application.web.dto.UploadDocumentResponse;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.JpaDocumentRepository;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.JpaFieldRepository;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.shaded.org.awaitility.core.ConditionTimeoutException;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
public class DocumentUploadAndProcessingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private JpaDocumentRepository documentRepository;

    @Autowired
    private JpaFieldRepository fieldRepository;

    @Value("${kafka.topics.document-uploaded}")
    private String documentUploadedTopic;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("apache/kafka:4.0.0")
    )
        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
        .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
        .withReuse(true);

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

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> grobid = new GenericContainer<>(
        DockerImageName.parse("lfoppiano/grobid:latest-crf")
    )
        .withExposedPorts(8070)
        .waitingFor(Wait.forHttp("/api/isalive").forStatusCode(200));

    @Container
    static OllamaContainer ollama = new OllamaContainer("ollama/ollama:0.11.8");

    private Consumer<String, DocumentUploadedMessage> kafkaConsumer;

    @Test
    void shouldProcessDocumentEndToEnd() throws Exception {
        var docUUID = uploadDocument("/test-files/bitcoin.pdf");

        var kafkaEvent = waitForKafkaEvent(docUUID);
        assertThat(kafkaEvent).isPresent();
        assertThat(kafkaEvent.get().documentID()).isEqualTo(docUUID);
        assertThat(kafkaEvent.get().filename()).endsWith(".pdf");
        assertThat(kafkaEvent.get().contentType()).isEqualTo(
            MediaType.APPLICATION_PDF_VALUE
        );
        assertThat(kafkaEvent.get().fileSizeBytes()).isGreaterThan(0);

        assertFileExistsInS3(kafkaEvent.get().bucketPath());

        assertDocumentExistsInDatabase(docUUID);
    }

    @Test
    void shouldRejectInvalidFileType() throws Exception {}

    @BeforeAll
    static void beforeAll() {
        kafka.start();
        postgres.start();
        minio.start();

        grobid.start();

        ollama.start();

        try {
            ollama.execInContainer("ollama", "pull", "nomic-embed-text:latest");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void afterAll() {
        kafka.stop();
        postgres.stop();
        minio.stop();
        grobid.stop();
        ollama.stop();
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

    private UUID uploadDocument(String filePath) {
        var document = new ClassPathResource(filePath);
        assertThat(document.exists()).isTrue();

        var parts = new LinkedMultiValueMap<>();
        parts.add("file", document);

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
        var docUUID = UUID.fromString(documentID);
        assertThat(docUUID).isNotNull();

        return docUUID;
    }

    private void assertDocumentExistsInDatabase(UUID id) {
        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(1))
            .until(() -> documentRepository.findById(id).isPresent());

        var document = documentRepository
            .findById(id)
            .orElseThrow(() ->
                new AssertionError(
                    "document with id [%s] not found in database".formatted(id)
                )
            );


        log.info("this is the document: {}", document);
        assertThat(document).isNotNull();
    }

    private void assertFileExistsInS3(String s3Path) {
        try {
            log.info(
                "Verifying file in S3 - Bucket: {}, Key: {}",
                bucketName,
                s3Path
            );

            var headResponse = s3Client.headObject(builder -> {
                builder.bucket(bucketName).key(s3Path);
            });

            assertThat(headResponse.contentLength()).isGreaterThan(0);
            assertThat(headResponse.contentType()).isEqualTo(
                MediaType.APPLICATION_PDF_VALUE
            );
        } catch (Exception e) {
            throw new AssertionError(
                "File should exist in s3 at path [%s]: %s".formatted(
                    s3Path,
                    e.getMessage()
                )
            );
        }
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
        consumerProps.put(
            ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG,
            "resolve_canonical_bootstrap_servers_only"
        );

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
