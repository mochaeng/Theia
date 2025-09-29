package com.mochaeng.theia_api.ingestion.infrastructure.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorizeHttp -> {
                authorizeHttp.anyRequest().authenticated();
            })
            .oauth2ResourceServer(
                oauth2ResourceServer ->
                    oauth2ResourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter()
                        )
                    )
                //                oauth2ResourceServer.jwt(Customizer.withDefaults())
            )
            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint((req, resp, e) -> {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        resp
                            .getWriter()
                            .write(
                                """
                                { "message": "You must be authenticated to access this resource" }
                                """
                            );
                    })
                    .accessDeniedHandler((req, resp, e) -> {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        resp
                            .getWriter()
                            .write(
                                """
                                { "message": "You do not have permission to perform this action" }
                                """
                            );
                    })
            )
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });

        return converter;


//        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//
//        grantedAuthoritiesConverter.setAuthoritiesClaimName(
//            "realm_access.roles"
//        );
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//
//        var authenticationConverter = new JwtAuthenticationConverter();
//        authenticationConverter.setJwtGrantedAuthoritiesConverter(
//            grantedAuthoritiesConverter
//        );
//
//        return authenticationConverter;
    }
}
