package com.goldeelox.gridserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.goldeelox.gridserver.websocket.GridWebSocketHandler;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    private final GridWebSocketHandler gridWebSocketHandler;

    public WebSocketConfig(GridWebSocketHandler gridWebSocketHandler) {
        this.gridWebSocketHandler = gridWebSocketHandler;
    }

    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws", gridWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1);
        mapping.setUrlMap(map);

        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
    
    @PostConstruct
    public void confirmInitialization() {
        System.out.println("✅ WebSocketConfig initialized");
    }
}