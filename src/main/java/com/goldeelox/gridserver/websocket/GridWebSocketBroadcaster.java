package com.goldeelox.gridserver.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GridWebSocketBroadcaster {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Sinks.Many<String> sink = Sinks.many().multicast().directBestEffort();

    public void register(WebSocketSession session) {
        sessions.add(session);
        session.closeStatus().doOnTerminate(() -> sessions.remove(session)).subscribe();
    }

    public void broadcast(String message) {
        sink.tryEmitNext(message);
    }

    public Flux<String> getBroadcastFlux() {
        return sink.asFlux();
    }
}