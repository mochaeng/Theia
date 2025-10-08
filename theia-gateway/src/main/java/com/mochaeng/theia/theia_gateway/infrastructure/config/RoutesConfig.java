package com.mochaeng.theia.theia_gateway.infrastructure.config;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.*;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Slf4j
@Configuration
public class RoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route("upload_with_limits")
            .POST("/api/v1/upload-document", http())
            .before(uri("http://localhost:8081/api/v1/upload-document"))
            .filter(
                rateLimit(config ->
                    config
                        .setCapacity(15)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(this::resolveUserKey)
                )
            )
            .build()
            .and(
                route("websocket_connection")
                    .GET("/ws/**", http())
                    .before(uri("http://localhost:8081/ws"))
                    .build()
            );
    }

    private String resolveUserKey(ServerRequest request) {
        return request.servletRequest().getUserPrincipal().getName();
    }

    //    private String resolveUserKey(ServerRequest request) {
    //        log.info("processing rate limit for request [{}]", request.path());
    //
    //        var auth = (Authentication) request.servletRequest().getUserPrincipal();
    //        if (auth instanceof JwtAuthenticationToken jwtAuthenticationToken) {
    //            var jwt = jwtAuthenticationToken.getToken();
    //            var userID = jwt.getClaimAsString("sub");
    //            if (userID != null) {
    //                return userID;
    //            }
    //        }
    //
    //        log.warn("unable to resolve user ID from JWT token");
    //        return null;
    //    }
}

//                        .setConfigurationBuilder(rateLimitConfig ->
//                            BucketConfiguration.builder()
//                                .addLimit(
//                                    Bandwidth.builder()
//                                        .capacity(rateLimitConfig.getCapacity())
//                                        .refillIntervally(
//                                            rateLimitConfig.getCapacity(),
//                                            rateLimitConfig.getPeriod()
//                                        )
//                                        .initialTokens(
//                                            rateLimitConfig.getCapacity()
//                                        ) // START WITH FULL BUCKET!
//                                        .build()
//                                )
//                                .build()
//                        )
