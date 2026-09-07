package com.infernokun.infernoctf.websocket;

import com.infernokun.infernoctf.services.WebSocketTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

/** Rejects WebSocket handshakes that do not present a valid ticket. */
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    /** Key under which the authenticated user id is published to the handler. */
    public static final String USER_ID_ATTRIBUTE = "userId";

    private final WebSocketTicketService ticketService;

    public WebSocketAuthInterceptor(WebSocketTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String ticket = UriComponentsBuilder.fromUri(request.getURI())
                .build().getQueryParams().getFirst("ticket");

        Optional<String> userId = ticketService.redeem(ticket);
        if (userId.isEmpty()) {
            log.debug("Rejected WebSocket handshake: missing or invalid ticket");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(USER_ID_ATTRIBUTE, userId.get());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
