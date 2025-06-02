package com.goldeelox.gridserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Service
public class WebSocketHandlerService implements WebSocketHandler {
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Simple echo server for now to validate full pipe:
        return session.receive()
                .doOnNext(message -> {
                    String payload = message.getPayloadAsText();
                    System.out.println("Received: " + payload);
                })
                .then();
    }
}