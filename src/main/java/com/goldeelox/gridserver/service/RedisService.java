package com.goldeelox.gridserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.goldeelox.gridserver.model.Cell;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private final WebClient webClient;

    public RedisService(
            @Value("${UPSTASH_REDIS_REST_URL}") String redisUrl,
            @Value("${UPSTASH_REDIS_REST_TOKEN}") String redisToken) {
        if (redisUrl == null || redisUrl.isBlank() || redisToken == null || redisToken.isBlank()) {
            throw new IllegalStateException("Missing required Redis env vars");
        }
        this.webClient = WebClient.builder()
                .baseUrl(redisUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, redisToken)
                .build();
    }

    public Mono<Void> saveCell(Cell cell) {
        String key = getKey(cell.getX(), cell.getY());
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/set/{key}/{value}")
                        .build(key, cell.getColor()))
                .retrieve()
                .bodyToMono(String.class)
                .then();
    }

    public Mono<Map<String, String>> getCellsInArea(int x1, int y1, int x2, int y2) {
        return Flux.range(x1, x2 - x1 + 1)
                .flatMap(x -> Flux.range(y1, y2 - y1 + 1)
                        .flatMap(y -> {
                            String key = getKey(x, y);
                            return webClient.get()
                                    .uri(uriBuilder -> uriBuilder.path("/get/{key}").build(key))
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .map(value -> Map.entry(formatKey(x, y), parseUpstashValue(value)))
                                    .defaultIfEmpty(Map.entry(formatKey(x, y), null));
                        }))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String parseUpstashValue(String response) {
        if (response == null) return null;
        return response.replace("\"", "");
    }

    private String getKey(int x, int y) {
        return "cell:" + x + ":" + y;
    }

    private String formatKey(int x, int y) {
        return x + "," + y;
    }

    public Mono<Boolean> ping() {
        return webClient.get()
                .uri("/ping")
                .retrieve()
                .bodyToMono(String.class)
                .map(pong -> pong.equalsIgnoreCase("PONG"))
                .onErrorResume(e -> {
                    System.err.println("❌ Redis PING failed: " + e.getMessage());
                    return Mono.just(false);
                });
    }
}