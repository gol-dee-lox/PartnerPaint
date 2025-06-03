package com.goldeelox.gridserver.controller;

import com.goldeelox.gridserver.model.Cell;
import com.goldeelox.gridserver.service.RedisService;

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GridController {

    private final RedisService redisService;

    public GridController(RedisService redisService) {
        this.redisService = redisService;
    }

    @GetMapping("/cells")
    public Mono<Map<String, String>> getCells(@RequestParam("x1") int x1, @RequestParam("y1") int y1,
                                              @RequestParam("x2") int x2, @RequestParam("y2") int y2) {
        return Mono.just(redisService.getCellsInArea(x1, y1, x2, y2));
    }

    @PostMapping("/cell")
    public Mono<Void> saveCell(@RequestBody Cell cell) {
        redisService.saveCell(cell);
        return Mono.empty();
    }
}