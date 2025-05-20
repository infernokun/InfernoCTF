package com.infernokun.infernoctf.config.websocket;

import com.infernokun.infernoctf.websocket.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// doc: https://docs.spring.io/spring-framework/reference/web/websocket/server.html

@Configuration
@EnableWebSocket
@EnableScheduling
public class ServerConfig implements WebSocketConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);
    @Bean
    public ServerHandler ctfWebSocketHandler() {
        return new ServerHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ctfWebSocketHandler(), "/socket/**")
                .setAllowedOrigins("*");
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        ctfWebSocketHandler().sendHeartbeatToClients();
    }
}
