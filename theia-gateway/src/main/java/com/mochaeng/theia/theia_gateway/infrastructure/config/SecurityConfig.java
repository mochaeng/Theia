package com.mochaeng.theia.theia_gateway.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorizeHttp -> {
                authorizeHttp.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(Customizer.withDefaults())
            )
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .build();
    }
}
