package com.mochaeng.theia_api.notification.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            var authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                var token = authHeader.substring(7);
                try {
                    var jwt = jwtDecoder.decode(token);
                    var auth = jwtAuthenticationConverter.convert(jwt);

                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug(
                        "authenticated websocket user '{}'",
                        auth.getName()
                    );
                } catch (Exception e) {
                    log.warn(
                        "invalid jwt for websocket connection: {}",
                        e.getMessage()
                    );
                    throw new IllegalArgumentException("Invalid token");
                }
            } else {
                log.warn("missing authorization header in stomp connect frame");
                throw new IllegalArgumentException(
                    "Missing Authorization header"
                );
            }
        }

        return message;
    }
}
