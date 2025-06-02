package com.goldeelox.gridserver.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.goldeelox.gridserver.model.Cell;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> saveCell(Cell cell) {
        String key = getKey(cell.getX(), cell.getY());
        return redisTemplate.opsForValue().set(key, cell.getColor()).then();
    }

    public Mono<Map<String, String>> getCellsInArea(int x1, int y1, int x2, int y2) {
        return Flux.range(x1, x2 - x1 + 1)
                .flatMap(x -> Flux.range(y1, y2 - y1 + 1)
                        .flatMap(y -> {
                            String key = getKey(x, y);
                            return redisTemplate.opsForValue().get(key)
                                    .map(value -> Map.entry(formatKey(x, y), value))
                                    .defaultIfEmpty(Map.entry(formatKey(x, y), null));
                        }))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getKey(int x, int y) {
        return "cell:" + x + ":" + y;
    }

    private String formatKey(int x, int y) {
        return x + "," + y;  // This is how the frontend expects keys.
    }
}