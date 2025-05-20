package com.infernokun.websocket;

import com.infernokun.config.InfernoCTFConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infernokun.config.websocket.ClientConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
@EnableWebSocket
@EnableScheduling
public class ClientHandler extends AbstractWebSocketHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private WebSocketConnectionManager manager = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ClientHandler(
            ClientConfig clientConfig,
            InfernoCTFConfig infernoCTFConfig) {

        WebSocketClient client = clientConfig.establishClient();
        this.manager = new WebSocketConnectionManager(
                client,
                this,
                infernoCTFConfig.getChatSocket()
        );
        manager.setAutoStartup(false);
    }

    @Bean
    public WebSocketConnectionManager getWebSocketConnectionManager() {
        return this.manager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("WEBSOCKET connection established: {}", session.getId());
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
        LOGGER.error("WebSocket transport error", exception);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        LOGGER.error("Socket msg: {}", message.getPayload());
    }

    //@Scheduled(fixedDelay = 60000) // Attempt reconnection every minute
    public void reconnect() {
        if (!manager.isRunning() || !manager.isConnected()) {
            LOGGER.info("Attempting to reconnect...");
            manager.start();
        }
    }

    private JsonNode parseString(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}