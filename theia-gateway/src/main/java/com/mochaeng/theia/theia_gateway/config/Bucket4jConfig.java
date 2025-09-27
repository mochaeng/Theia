package com.mochaeng.theia.theia_gateway.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;


import java.time.Duration;

@Configuration
public class Bucket4jConfig {

    @Value("${redis.rate.limiter.address}")
    private String rateLimiterAddress;


    @Bean(destroyMethod = "shutdown")
    public RedisClient getRedisClient() {
        return RedisClient.create(rateLimiterAddress);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> getRedisConnection(
        RedisClient redisClient
    ) {
        var codec = RedisCodec.of(StringCodec.UTF8, new ByteArrayCodec());
        return redisClient.connect(codec);
    }

    @Bean
    public AsyncProxyManager<String> proxyManager(
        StatefulRedisConnection<String, byte[]> connection
    ) {
        var expirationAfterWrite = ExpirationAfterWriteStrategy.fixedTimeToLive(
            Duration.ofMinutes(1)
        );

        return Bucket4jLettuce.casBasedBuilder(connection)
            .expirationAfterWrite(expirationAfterWrite)
            .build()
            .asAsync();
    }
}
