package com.infernokun.infernoctf.config;

import com.infernokun.infernoctf.services.WebSocketTicketService;
import com.infernokun.infernoctf.websocket.SocketHandler;
import com.infernokun.infernoctf.websocket.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

@Configuration
@EnableWebSocket
// Required by SocketHandler's heartbeat and WebSocketTicketService's cleanup.
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketTicketService ticketService;
    private final String[] allowedOrigins;

    public WebSocketConfig(WebSocketTicketService ticketService,
                           @Value("${app.cors.allowed-origins:http://localhost:4301}") String allowedOrigins) {
        this.ticketService = ticketService;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Bean
    public SocketHandler socketHandler() {
        return new SocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler(), "/socket-handler/update")
                .addInterceptors(new WebSocketAuthInterceptor(ticketService))
                .setAllowedOrigins(allowedOrigins);
    }
}
