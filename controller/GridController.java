package com.goldeelox.gridserver.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cell")
public class GridController {

    private final RedisOperations<String, String> redisOps;

    public GridController(@Qualifier("redisOperations") RedisOperations<String, String> redisOperations) {
        this.redisOps = redisOperations;
    }

    private String getKey(int x, int y) {
        return "cell:" + x + ":" + y;
    }

    @GetMapping("/{x}/{y}")
    public Mono<String> getCell(@PathVariable int x, @PathVariable int y) {
        return Mono.fromSupplier(() -> redisOps.opsForValue().get(getKey(x, y)))
                   .flatMap(future -> Mono.fromFuture(future))
                   .defaultIfEmpty("default");
    }

    @PostMapping("/{x}/{y}")
    public Mono<Void> setCell(@PathVariable int x, @PathVariable int y, @RequestBody String color) {
        return Mono.fromSupplier(() -> redisOps.opsForValue().set(getKey(x, y), color))
                   .flatMap(future -> Mono.fromFuture(future))
                   .then();
    }
}