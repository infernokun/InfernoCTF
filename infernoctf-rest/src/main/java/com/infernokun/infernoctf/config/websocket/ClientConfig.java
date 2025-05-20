package com.infernokun.infernoctf.config.websocket;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class ClientConfig {

    @Bean
    public StandardWebSocketClient establishClient() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        return new StandardWebSocketClient(container);
    }
}
