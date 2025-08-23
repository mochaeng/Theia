package com.mochaeng.theia_api.shared.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3Config {

  @Bean
  public S3Client s3Client(S3Properties props) {
    AwsBasicCredentials credentials =
        AwsBasicCredentials.create(props.accessKey(), props.secretAccessKey());

    S3ClientBuilder clientBuilder =
        S3Client.builder()
            .region(Region.of(props.region()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .forcePathStyle(props.pathStyleAccess());

    if (props.endpoint() != null && !props.endpoint().trim().isEmpty()) {
      clientBuilder.endpointOverride(URI.create(props.endpoint()));
    }

    return clientBuilder.build();
  }
}
