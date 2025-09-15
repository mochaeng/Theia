package com.mochaeng.theia_api.shared.infrastructure.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record S3Properties(
    String bucketName,
    String accessKey,
    String secretAccessKey,
    String region,
    boolean pathStyleAccess,
    String endpoint
) {}
