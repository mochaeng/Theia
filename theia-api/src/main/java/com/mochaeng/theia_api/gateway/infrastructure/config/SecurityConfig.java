//package com.mochaeng.theia_api.gateway.infrastructure.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    @Value("${app.jwk.uri}")
//    private String jwkUri;
//
//    @Bean
//    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        return http
//            .authorizeHttpRequests(authorizeHttp -> {
//                authorizeHttp.anyRequest().authenticated();
//            })
//            .oauth2ResourceServer(oauth2ResourceServer ->
//                //                oauth2ResourceServer.jwt(Customizer.withDefaults())
//                oauth2ResourceServer.jwt(jwtConfigurer ->
//                    jwtConfigurer.decoder(jwtDecoder())
//                )
//            )
//            .sessionManagement(sessionManagement ->
//                sessionManagement.sessionCreationPolicy(
//                    SessionCreationPolicy.STATELESS
//                )
//            )
//            .build();
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        return NimbusJwtDecoder.withJwkSetUri(jwkUri).build();
//    }
//}
