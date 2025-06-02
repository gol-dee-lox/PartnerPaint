package com.goldeelox.gridserver.controller;

import com.goldeelox.gridserver.websocket.GridWebSocketHandler;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.*;

@RestController
public class CellController {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    private GridWebSocketHandler websocketHandler;
    
//    @PostConstruct
//    public void testRedisConnection() {
//        redisTemplate.opsForValue().set("testKey", "testValue")
//            .doOnSuccess(v -> System.out.println("✅ Successfully wrote to Redis"))
//            .then(redisTemplate.opsForValue().get("testKey"))
//            .subscribe(val -> System.out.println("✅ Successfully read from Redis: " + val));
//    }
    
    
    // Store updated cell into Redis
    @PostMapping("/cell/{x}/{y}")
    public Mono<Void> updateCell(@PathVariable("x") int x, @PathVariable("y") int y, @RequestBody(required = false) String color) {
        //System.out.printf("✅ CellController called: (%d, %d) = %s%n", x, y, color);
        String key = String.format("cell:%d:%d", x, y);
        return redisTemplate.opsForValue().set(key, color)
                .then(Mono.fromRunnable(() -> {
                    String message = String.format("{\"type\":\"cellUpdate\",\"x\":%d,\"y\":%d,\"color\":\"%s\"}", x, y, color);
                    websocketHandler.broadcastCellUpdate(message);
                }))
                .then();
    }

    // Retrieve previously stored cells for frontend redraw
    @GetMapping("/cells")
    public Mono<Map<String, String>> getCells(
            @RequestParam("x1") int x1, @RequestParam("y1") int y1,
            @RequestParam("x2") int x2, @RequestParam("y2") int y2) {

        //System.out.printf("✅ Fetching region: (%d,%d) to (%d,%d)%n", x1, y1, x2, y2);

        List<String> keys = new ArrayList<>();
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                keys.add(String.format("cell:%d:%d", x, y));  // <-- ADD "cell:" prefix here!
            }
        }

        return redisTemplate.opsForValue().multiGet(keys)
                .map(values -> {
                    Map<String, String> result = new HashMap<>();
                    for (int i = 0; i < keys.size(); i++) {
                        if (values.get(i) != null) {
                            result.put(keys.get(i), values.get(i));
                            //System.out.println("✅ Redis returned: " + keys.get(i) + " -> " + values.get(i));
                        }
                    }
                    return result;
                });
    }
}