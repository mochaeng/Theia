package com.mochaeng.theia_api.shared.infrastructure.s3;

import java.net.URI;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties props) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            props.accessKey(),
            props.secretAccessKey()
        );

        ClientOverrideConfiguration overrideConfig =
            ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(5))
                .apiCallAttemptTimeout(Duration.ofMinutes(2))
                .retryStrategy(RetryMode.STANDARD)
                .build();

        S3ClientBuilder clientBuilder = S3Client.builder()
            .region(Region.of(props.region()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .overrideConfiguration(overrideConfig)
            .forcePathStyle(props.pathStyleAccess());

        if (props.endpoint() != null && !props.endpoint().trim().isEmpty()) {
            clientBuilder.endpointOverride(URI.create(props.endpoint()));
        }

        return clientBuilder.build();
    }
}
