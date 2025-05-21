package com.infernokun.infernoctf.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infernokun.infernoctf.interfaces.JsonDate;
import com.infernokun.infernoctf.models.entities.StoredObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class SocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> currentSessions = new CopyOnWriteArrayList<>();
    private final ObjectWriter writer;

    public SocketHandler() {
        writer = new ObjectMapper().writerFor(HeartbeatDTO.class);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.currentSessions.add(session);
        log.info("WEBSOCKET Connection Established w ID: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.currentSessions.remove(session);
        log.info("WEBSOCKET Connection Closed for ID: {}", session.getId());
    }

    public void broadcastObjectUpdate(StoredObject storedObject) {
        for (WebSocketSession session : this.currentSessions) {
            try {
                if (session.isOpen()) {
                    ObjectNode storedObjectJsonObject = new ObjectMapper().readValue(storedObject.toString(), ObjectNode.class);

                    String[] classNameParts = storedObject.getClass().getName().split("\\.");
                    storedObjectJsonObject.put("name", classNameParts[classNameParts.length - 1]);

                    TextMessage storedObjectMessage = new TextMessage(storedObjectJsonObject.toString());
                    session.sendMessage(storedObjectMessage);
                } else {
                    log.warn("WEBSOCKET Session is not open: {}", session.getId());
                }
            } catch (IOException ex) {
                log.warn("WEBSOCKET Error sending message to session {}: {}", session.getId(), ex.getMessage());
            }
        }
    }

    @Scheduled(fixedRateString = "${SOCKET_POLL_MS:30000}")
    public void sendHeartbeatToClients() {
        for (WebSocketSession session : this.currentSessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(writer.writeValueAsString(
                            HeartbeatDTO
                                    .builder()
                                    .type("heartbeat")
                                    .timestamp(LocalDateTime.now())
                                    .session(session.getId())
                                    .build())));
                }
            } catch (IOException e) {
                log.warn("WEBSOCKET issue sending heartbeat: {}", session.getId(), e);
            }
        }
    }

    @Data
    @Builder
    private static class HeartbeatDTO {
        private String type;
        @JsonDate
        private LocalDateTime timestamp;
        private String session;
    }
}
