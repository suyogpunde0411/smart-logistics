package com.smartlogistics.socket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.smartlogistics.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocketIOServerConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOServerConfig.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final LocationSocketHandler locationSocketHandler;

    @Value("${app.socket.host:0.0.0.0}")
    private String host;

    @Value("${app.socket.port:5001}")
    private int port;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin(null);
        config.setTransports(com.corundumstudio.socketio.Transport.WEBSOCKET, com.corundumstudio.socketio.Transport.POLLING);
        config.setJsonSupport(new com.corundumstudio.socketio.protocol.JacksonJsonSupport(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));

        // Auth listener
        config.setAuthorizationListener(handshakeData -> {
            try {
                Object authObj = handshakeData.getAuthToken();
                String token = null;

                if (authObj instanceof String strToken) {
                    token = strToken;
                } else if (authObj instanceof java.util.Map<?, ?> authMap) {
                    Object tokenVal = authMap.get("token");
                    if (tokenVal != null) {
                        token = tokenVal.toString();
                    }
                }

                if (token == null) {
                    token = handshakeData.getSingleUrlParam("token");
                }

                if (token == null || !jwtTokenProvider.validateToken(token)) {
                    log.warn("Socket.IO connection rejected: Invalid or missing token");
                    return com.corundumstudio.socketio.AuthorizationResult.FAILED_AUTHORIZATION;
                }

                return com.corundumstudio.socketio.AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
            } catch (Exception e) {
                log.warn("Socket.IO auth check exception: {}", e.getMessage());
                return com.corundumstudio.socketio.AuthorizationResult.FAILED_AUTHORIZATION;
            }
        });

        server = new SocketIOServer(config);

        // Add handshake data extractor on connect
        server.addConnectListener(client -> {
            try {
                Object authObj = client.getHandshakeData().getAuthToken();
                String token = null;

                if (authObj instanceof String strToken) {
                    token = strToken;
                } else if (authObj instanceof java.util.Map<?, ?> authMap) {
                    Object tokenVal = authMap.get("token");
                    if (tokenVal != null) {
                        token = tokenVal.toString();
                    }
                }

                if (token == null) {
                    token = client.getHandshakeData().getSingleUrlParam("token");
                }

                if (token != null && jwtTokenProvider.validateToken(token)) {
                    Claims claims = jwtTokenProvider.getClaims(token);
                    String userId = claims.get("id", String.class);
                    if (userId == null) userId = claims.getSubject();
                    String role = claims.get("role", String.class);

                    client.set("userId", userId);
                    client.set("role", role);
                    locationSocketHandler.onConnect(client);
                }
            } catch (Exception e) {
                log.error("Error during socket onConnect listener: {}", e.getMessage());
            }
        });

        server.addDisconnectListener(locationSocketHandler::onDisconnect);

        // Register Spring-managed handler annotations
        server.addListeners(locationSocketHandler);

        try {
            server.start();
            log.info("Netty-SocketIO server started successfully on {}:{}", host, port);
        } catch (Exception e) {
            log.error("Failed to start Netty-SocketIO server on port {}: {}", port, e.getMessage());
        }

        return server;
    }

    @PreDestroy
    public void stopSocketIOServer() {
        if (server != null) {
            server.stop();
            log.info("Netty-SocketIO server stopped");
        }
    }
}
