package com.goldeelox.gridserver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import org.springframework.web.reactive.socket.WebSocketHandler;
import reactor.core.publisher.*;
import reactor.util.concurrent.Queues;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GridWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, PlayerInfo> players = new ConcurrentHashMap<>();
    private final Sinks.Many<String> broadcastSink = Sinks.many().multicast().directBestEffort();

    private final AtomicLong idCounter = new AtomicLong(1);
    private final ReactiveStringRedisTemplate redisTemplate;
    
    @Autowired
    public GridWebSocketHandler(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        //System.out.println("✅ WebSocket handler instance created");

        String id = String.valueOf(idCounter.getAndIncrement());
        sessions.put(id, session);
        System.out.println("✅ Assigned player ID: " + id);

        // Combine initial assignment message + ongoing outbound stream
        Flux<WebSocketMessage> outbound = Flux.concat(
                Mono.just(session.textMessage("{\"type\":\"assignId\",\"id\":\"" + id + "\"}")),
                broadcastSink.asFlux().map(session::textMessage)
        ).onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE);

        Flux<String> receiveFlux = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> {
                    //System.out.println("📩 Incoming message for id " + id + ": " + message);
                    handleMessage(id, message);
                })
                .doOnError(e -> e.printStackTrace())
                .doFinally(sig -> {
                    System.out.println("⚠️ Closing session for id " + id);
                    sessions.remove(id);
                    players.remove(id);
                    broadcastPlayerDisconnect(id);
                });

        return session.send(outbound).and(receiveFlux.then());
    }

    private void handleMessage(String id, String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String type = (String) payload.get("type");

            if ("position".equals(type)) {
                String username = (String) payload.get("username");
                int x = (int) payload.get("x");
                int y = (int) payload.get("y");

                players.put(id, new PlayerInfo(username, x, y));

                // Broadcast position to everyone in real time
                Map<String, Object> update = new HashMap<>();
                update.put("type", "positionUpdate");
                update.put("id", id);
                update.put("username", username);
                update.put("x", x);
                update.put("y", y);

                String json = objectMapper.writeValueAsString(update);
                //System.out.println("✅ Broadcasting positionUpdate: " + json);
                broadcastSink.tryEmitNext(json);
                //System.out.println("🚀 Broadcasting to all: " + json);
            }

            else if ("cell".equals(type)) {
                int gx = (int) payload.get("gx");
                int gy = (int) payload.get("gy");
                String color = (String) payload.get("color");

                Map<String, Object> cellUpdate = new HashMap<>();
                cellUpdate.put("type", "cell");
                cellUpdate.put("gx", gx);
                cellUpdate.put("gy", gy);
                cellUpdate.put("color", color);

                String json = objectMapper.writeValueAsString(cellUpdate);
                //System.out.println("✅ Broadcasting cell update: " + json);
                broadcastSink.tryEmitNext(json);
                //System.out.println("🚀 Broadcasting to all: " + json);
            }
            
            //System.out.println("📡 Received message from " + id + ": " + message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastPlayerDisconnect(String id) {
        try {
            Map<String, Object> disconnect = new HashMap<>();
            disconnect.put("type", "disconnect");
            disconnect.put("id", id);
            String json = objectMapper.writeValueAsString(disconnect);
            //System.out.println("✅ Broadcasting disconnect: " + json);
            broadcastSink.tryEmitNext(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PlayerInfo {
        String username;
        int x;
        int y;

        public PlayerInfo(String username, int x, int y) {
            this.username = username;
            this.x = x;
            this.y = y;
        }
    }
    
    public void broadcastCellUpdate(String message) {
        broadcastSink.tryEmitNext(message);
    }
}