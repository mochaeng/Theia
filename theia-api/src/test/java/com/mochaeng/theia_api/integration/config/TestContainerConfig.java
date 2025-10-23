package com.mochaeng.theia_api.integration.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.io.IOException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainerConfig {

    @Container
    protected static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("apache/kafka:4.0.0")
    )
        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
        .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
        .withReuse(true);

    @Container
    protected static MinIOContainer minio = new MinIOContainer(
        "minio/minio:RELEASE.2025-07-18T21-56-31Z"
    );

    @Container
    protected static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(
            DockerImageName.parse(
                "pgvector/pgvector:pg17-trixie"
            ).asCompatibleSubstituteFor("postgres")
        );

    @Container
    @SuppressWarnings("resource")
    protected static GenericContainer<?> grobid = new GenericContainer<>(
        DockerImageName.parse("lfoppiano/grobid:latest-crf")
    )
        .withExposedPorts(8070)
        .waitingFor(Wait.forHttp("/api/isalive").forStatusCode(200));

    @Container
    protected static OllamaContainer ollama = new OllamaContainer(
        "ollama/ollama:0.11.8"
    );

    @Container
    protected static KeycloakContainer keycloak = new KeycloakContainer(
        "keycloak/keycloak:26.4"
    );

    protected static void pullModel(OllamaContainer container, String modelName)
        throws IOException, InterruptedException {
        container.execInContainer("ollama", "pull", modelName);
    }

    //    static {
    //        try {
    //            ollama.execInContainer("ollama", "pull", "nomic-embed-text:latest");
    //        } catch (Exception e) {
    //            throw new RuntimeException("Failed to pull Ollama model", e);
    //        }
    //    }
}
