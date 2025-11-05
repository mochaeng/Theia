package com.mochaeng.theia.reactive_gateway.infrastructure.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;


@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges ->
                exchanges
                    .pathMatchers("/ws/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            )
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(
                    Customizer.withDefaults()
                )
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((exchange, denied) -> {
                    log.info("access denied for path '{}'", exchange.getRequest().getPath());
                    return Mono.error(denied);
                })
            )
            .build();
    }


//    @Bean
//    public Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveJwtAuthenticationConverter() {
//        var converter = new JwtAuthenticationConverter();
//
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//            if (realmAccess == null || !realmAccess.containsKey("roles")) {
//                return List.of();
//            }
//
//            var rolesObj = realmAccess.get("roles");
//            if (!(rolesObj instanceof List<?> roles)) {
//                return List.of();
//            }
//
//            return roles
//                .stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                .collect(Collectors.toList());
//        });
//
//        return new ReactiveJwtAuthenticationConverterAdapter(converter);
//    }
}
