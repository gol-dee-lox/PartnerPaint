package com.goldeelox.gridserver.controller;

import com.goldeelox.gridserver.model.Cell;
import com.goldeelox.gridserver.service.RedisService;
import com.goldeelox.gridserver.websocket.GridWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class CellController {

    private final RedisService redisService;
    private final GridWebSocketHandler websocketHandler;

    @Autowired
    public CellController(RedisService redisService, GridWebSocketHandler websocketHandler) {
        this.redisService = redisService;
        this.websocketHandler = websocketHandler;
    }

    // Store updated cell into Redis
    @PostMapping("/cell/{x}/{y}")
    public Mono<Void> updateCell(@PathVariable("x") int x, @PathVariable("y") int y, @RequestBody(required = false) String color) {
        // Build Cell object
        Cell cell = new Cell();
        cell.setX(x);
        cell.setY(y);
        cell.setColor(color);

        return redisService.saveCell(cell)
                .then(Mono.fromRunnable(() -> {
                    String message = String.format("{\"type\":\"cellUpdate\",\"x\":%d,\"y\":%d,\"color\":\"%s\"}", x, y, color);
                    websocketHandler.broadcastCellUpdate(message);
                }));
    }

    // Retrieve previously stored cells for frontend redraw
    @GetMapping("/cells")
    public Mono<Map<String, String>> getCells(
            @RequestParam("x1") int x1, @RequestParam("y1") int y1,
            @RequestParam("x2") int x2, @RequestParam("y2") int y2) {
        return redisService.getCellsInArea(x1, y1, x2, y2);
    }
}