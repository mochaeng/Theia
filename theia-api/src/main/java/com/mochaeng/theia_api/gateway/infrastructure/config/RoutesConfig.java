package com.mochaeng.theia_api.gateway.infrastructure.config;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.*;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

public class RoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route("upload_with_limits")
            .POST("/api/v1/upload-document", http())
            .before(uri("http://your-upload-service:8080"))
            .filter(
                rateLimit(config ->
                    config
                        .setCapacity(1)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(request ->
                            request.headers().firstHeader("User-ID")
                        )
                )
            )
            .build();
    }
}
