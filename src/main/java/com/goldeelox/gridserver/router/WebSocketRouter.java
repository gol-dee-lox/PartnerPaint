package com.goldeelox.gridserver.router;

import com.goldeelox.gridserver.service.WebSocketHandlerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class WebSocketRouter {

    @Bean
    public HandlerMapping webSocketHandlerMapping(WebSocketHandlerService webSocketHandlerService) {
        return new SimpleUrlHandlerMapping(
            Map.of("/ws", webSocketHandlerService),
            -1
        );
    }
}