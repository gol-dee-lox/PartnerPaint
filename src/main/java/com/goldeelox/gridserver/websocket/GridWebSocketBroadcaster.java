package com.goldeelox.gridserver.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GridWebSocketBroadcaster {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void register(WebSocketSession session) {
        sessions.add(session);
        session.closeStatus().doOnTerminate(() -> sessions.remove(session)).subscribe();
    }

    public void broadcast(String message) {
        sessions.forEach(session -> 
            session.send(Mono.just(session.textMessage(message)))
                .onErrorResume(e -> {
                    sessions.remove(session);
                    return Mono.empty();
                })
                .subscribe()
        );
    }
}