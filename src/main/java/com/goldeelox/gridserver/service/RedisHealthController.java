package com.goldeelox.gridserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goldeelox.gridserver.websocket.GridWebSocketHandler;

import reactor.core.publisher.Mono;

@RestController
public class RedisHealthController {

    @Autowired
    private GridWebSocketHandler websocketHandler;

    @GetMapping("/redis-health")
    public Mono<String> redisHealth() {
        return websocketHandler.ping().map(healthy -> healthy ? "✅ Redis healthy" : "❌ Redis problem");
    }
}