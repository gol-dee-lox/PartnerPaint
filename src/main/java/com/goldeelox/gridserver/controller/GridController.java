package com.goldeelox.gridserver.controller;

import com.goldeelox.gridserver.model.Cell;
import com.goldeelox.gridserver.service.RedisService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GridController {

    private final RedisService redisService;

    public GridController(RedisService redisService) {
        this.redisService = redisService;
    }

    @GetMapping("/cells")
    public Mono<Map<String, String>> getCells(
            @RequestParam int x1,
            @RequestParam int y1,
            @RequestParam int x2,
            @RequestParam int y2) {

        return redisService.getCellsInArea(x1, y1, x2, y2);
    }

    @PostMapping("/cell")
    public Mono<Void> saveCell(@RequestBody Cell cell) {
        return redisService.saveCell(cell);
    }
}