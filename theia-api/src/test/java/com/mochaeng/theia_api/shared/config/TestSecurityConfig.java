//package com.mochaeng.theia_api.shared.config;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@TestConfiguration
//public class TestSecurityConfig {
//    @Bean
//    @Primary
//    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
//        return http
//            .authorizeHttpRequests(authorizeHttp -> {
//                authorizeHttp.anyRequest().permitAll();  // Allow all in tests
//            })
//            .build();
//    }
//}
