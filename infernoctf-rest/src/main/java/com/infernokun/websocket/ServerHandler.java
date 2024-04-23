package com.infernokun.websocket;

import com.infernokun.models.sessions.SessionDomain;
import com.infernokun.models.sessions.SessionTracker;
import org.jetbrains.annotations.NotNull;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerHandler extends TextWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    private final List<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
    private final SessionTracker sessionTracker = new SessionTracker();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        List<String> parts = List.of(Objects.requireNonNull(session.getUri()).toString().split("/"));
        if (parts.isEmpty()) return;

        int topicIdx = parts.indexOf("topic");

        if (topicIdx == -1) {
            this.sessionList.add(session);

            LOGGER.info("WEBSOCKET SESSION ESTABLISHED: {}", session.getId());
        } else {
            if (parts.size() > topicIdx + 1) {
                switch (parts.get(topicIdx + 1)) {
                    case "chat-messages-all" -> this.sessionTracker.addDomainSession(session, "all");
                    case "chat-messages-domain" -> {
                        String domain = parts.get(topicIdx + 2);
                        this.sessionTracker.addDomainSession(session, domain);
                    }
                    case "chat-messages-room" -> {
                        String room_domain = parts.get(topicIdx + 2);
                        String room = parts.get(topicIdx + 3);
                        this.sessionTracker.addRoomSession(session, room_domain, room);
                    }
                    default -> LOGGER.warn("wrong answer!");
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        List<String> parts = List.of(Objects.requireNonNull(session.getUri()).toString().split("/"));
        if (parts.isEmpty()) return;

        int topicIdx = parts.indexOf("topic");

        if (topicIdx == -1) {
            super.afterConnectionClosed(session, status);
            this.sessionList.remove(session);

            LOGGER.info("WEBSOCKET SESSION TERMINATED: " + session.getId());
        } else {
            if (parts.size() > topicIdx + 1) {
                switch (parts.get(topicIdx + 1)) {
                    case "chat-messages-all" -> this.sessionTracker.removeDomainSession(session, "all");
                    case "chat-messages-domain" -> {
                        String domain = parts.get(topicIdx + 2);
                        this.sessionTracker.removeDomainSession(session, domain);
                    }
                    case "chat-messages-room" -> {
                        String room_domain = parts.get(topicIdx + 2);
                        String room = parts.get(topicIdx + 3);
                        this.sessionTracker.removeRoomSession(session, room_domain, room);
                    }
                    default -> LOGGER.warn("wrong answer");
                }
            }
        }
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        if ("\"ping\"".equals(message.getPayload())) {
            LOGGER.info(message.getPayload());
            session.sendMessage(new TextMessage("pong".getBytes()));
        }
    }

    public void sendMessage(Object something) {
        String domain = something.toString();
        String room = something.toString();

        String thingJson = something.toString();

        SessionDomain sessionDomain = this.sessionTracker.getDomainMap().get(domain);

        if (sessionDomain == null) return;

        this.sendMessageToSessions(sessionDomain.getDomainSessions(), thingJson);

        if (room != null) {
            List<WebSocketSession> roomSessions = sessionDomain.getRooms().get(room);
            if (roomSessions != null) {
                this.sendMessageToSessions(roomSessions, thingJson);
            }
        } else {
            sessionDomain
                    .getRooms()
                    .forEach((roomNames, roomSessions) -> this.sendMessageToSessions(roomSessions, thingJson));
        }
    }

    @SuppressWarnings("unchecked")
    public void sendHeartbeatToClients() {
        JSONObject heartbeatJson = new JSONObject();
        heartbeatJson.put("type", "heartbeat");
        heartbeatJson.put("timestamp", LocalDateTime.now().toString());

        this.sessionTracker.getDomainMap().forEach((domainNames, domainSessions) -> {
            this.sendMessageToSessions(domainSessions.getDomainSessions(), heartbeatJson.toJSONString());

            domainSessions.getRooms().forEach((roomNames, roomSessions) ->
                    this.sendMessageToSessions(roomSessions, heartbeatJson.toJSONString()));
        });
    }

    private void sendMessageToSessions(List<WebSocketSession> sessions, String jsonMessage) {
        sessions.stream().filter(WebSocketSession::isOpen).forEach(session -> {
            try {
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException ex) {
                LOGGER.warn("WEBSOCKET issue sending message: {}", session.getId());
            }
        });
    }
}
