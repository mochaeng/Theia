package com.mochaeng.theia_api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.mochaeng.theia_api.ingestion.application.web.UploadDocumentResponse;
import com.mochaeng.theia_api.integration.config.TestContainerConfig;
import com.mochaeng.theia_api.integration.helpers.KafkaTestHelper;
import com.mochaeng.theia_api.integration.helpers.KeycloakTestHelper;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.shared.infrastructure.jpa.JpaDocumentRepository;
import com.mochaeng.theia_api.shared.infrastructure.jpa.JpaFieldRepository;
import io.vavr.control.Option;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
public class DocumentUploadAndProcessingIntegrationTest
    extends TestContainerConfig {

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

    @Value("${storage.s3.incoming-bucket-name}")
    private String incomingBucket;

    private Consumer<String, DocumentMessage> kafkaConsumer;

    @Test
    void shouldProcessDocumentEndToEnd() throws Exception {
        var docUUID = assertUploadDocument("/test-files/bitcoin.pdf");

        var event = assertWaitForKafkaEvent(docUUID);
        assertThat(event).isNotNull();
        assertThat(event.documentID()).isEqualTo(docUUID);
        assertThat(event.contentType()).isEqualTo(
            MediaType.APPLICATION_PDF_VALUE
        );
        assertThat(event.fileSizeBytes()).isGreaterThan(0);

        assertFileExistsInS3(incomingBucket, event.key());
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        KeycloakTestHelper.setupRealm(keycloak);
        pullModel(ollama, "nomic-embed-text:latest");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.kafka.bootstrap-servers",
            kafka::getBootstrapServers
        );

        registry.add(
            "parser.grobid.base-url",
            () ->
                "http://" + grobid.getHost() + ":" + grobid.getMappedPort(8070)
        );

        registry.add("storage.s3.endpoint", minio::getS3URL);
        registry.add("storage.s3.access-key", minio::getUserName);
        registry.add("storage.s3.secret-access-key", minio::getPassword);
        registry.add("storage.s3.region", () -> "us-east-1");
        registry.add("storage.s3.path-style-access", () -> "true");

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add(
            "spring.security.oauth2.resourceserver.jwt.issuer-uri",
            () -> keycloak.getAuthServerUrl() + "/realms/testrealm"
        );
    }

    @BeforeEach
    void setUp() {
        createS3Bucket();
        kafkaConsumer = KafkaTestHelper.createConsumer(
            kafka,
            documentUploadedTopic
        );
    }

    private UUID assertUploadDocument(String filePath) {
        var document = new ClassPathResource(filePath);
        assertThat(document.exists()).isTrue();

        var parts = new LinkedMultiValueMap<>();
        parts.add("file", document);

        var accessToken = assertObtainAccessToken();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(accessToken);

        var requestEntity = new HttpEntity<>(parts, headers);
        var response = restTemplate.exchange(
            "http://localhost:" + port + "/api/v1/upload-document",
            HttpMethod.POST,
            requestEntity,
            UploadDocumentResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        assertThat(body).isNotNull();

        var documentID = body.documentID();
        assertThat(documentID).isNotNull();

        var docUUID = UUID.fromString(documentID);
        assertThat(docUUID).isNotNull();

        return docUUID;
    }

    private void assertFileExistsInS3(String bucket, String key) {
        var response = s3Client.headObject(builder ->
            builder.bucket(bucket).key(key)
        );

        assertThat(response.contentLength()).isGreaterThan(0);
        assertThat(response.contentType()).isEqualTo(
            MediaType.APPLICATION_PDF_VALUE
        );
    }

    private void createS3Bucket() {
        s3Client.createBucket(
            CreateBucketRequest.builder().bucket(incomingBucket).build()
        );
    }

    private DocumentMessage assertWaitForKafkaEvent(UUID documentID) {
        return await()
            .atMost(15, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(500))
            .until(
                () ->
                    KafkaTestHelper.pollForDocument(kafkaConsumer, documentID),
                Option::isDefined
            )
            .getOrElseThrow(() ->
                new AssertionError("polling document failed:")
            );
    }

    private String assertObtainAccessToken() {
        var tokenUrl =
            keycloak.getAuthServerUrl() +
            "/realms/testrealm/protocol/openid-connect/token";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "password");
        map.add("client_id", "test-client");
        map.add("client_secret", "test-secret");
        map.add("username", "uploaderUser");
        map.add("password", "password");

        var entity = new HttpEntity<>(map, headers);
        var response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());

        return (String) response.getBody().get("access_token");
    }

    //    private void assertDocumentExistsInDatabase(UUID id) {
    //        await()
    //            .atMost(Duration.ofMinutes(5))
    //            .pollInterval(Duration.ofSeconds(5))
    //            .until(() -> documentRepository.findById(id).isPresent());
    //
    //        var document = documentRepository
    //            .findById(id)
    //            .orElseThrow(() ->
    //                new AssertionError(
    //                    "document with id [%s] not found in database".formatted(id)
    //                )
    //            );
    //
    //        log.info("this is the document: {}", document);
    //        assertThat(document).isNotNull();
    //    }
}
