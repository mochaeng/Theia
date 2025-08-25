package com.mochaeng.theia_api;

import com.mochaeng.theia_api.shared.config.s3.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(S3Properties.class)
public class TheiaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheiaApiApplication.class, args);
    }
}
