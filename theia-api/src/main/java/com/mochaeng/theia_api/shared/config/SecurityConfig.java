package com.mochaeng.theia_api.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
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
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint(this::onAuthenticationEntryPoint)
                    .accessDeniedHandler(this::onAccessDenied)
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

            var rolesObj = realmAccess.get("roles");
            if (!(rolesObj instanceof List<?> roles)) {
                return List.of();
            }

            return roles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });

        return converter;
    }

    private void onAccessDenied(
        HttpServletRequest req,
        HttpServletResponse resp,
        AccessDeniedException e
    ) {
        writeJsonResponse(
            resp,
            HttpServletResponse.SC_FORBIDDEN,
            "You do not have permission to perform this action"
        );
    }

    private void onAuthenticationEntryPoint(
        HttpServletRequest req,
        HttpServletResponse resp,
        AuthenticationException e
    ) {
        writeJsonResponse(
            resp,
            HttpServletResponse.SC_UNAUTHORIZED,
            "You must be authenticated to access this resource"
        );
    }

    private void writeJsonResponse(
        HttpServletResponse resp,
        int status,
        String message
    ) {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = resp.getWriter()) {
            writer.write("{\"message\":\"" + message + "\"}");
            writer.flush();
        } catch (IOException e) {
            log.error("failed to write json response: {}", e.getMessage());
        }
    }
}
