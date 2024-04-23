package com.infernokun.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "infernoctf")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfernoCTFConfig {
    private String applicationName;
    private String chatService;
    private String chatSocket;
    private String apiKey;
}
