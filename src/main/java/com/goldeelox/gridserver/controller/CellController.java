package com.goldeelox.gridserver.controller;

import com.goldeelox.gridserver.model.Cell;
import com.goldeelox.gridserver.service.RedisService;
import com.goldeelox.gridserver.websocket.GridWebSocketHandler;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    
    @GetMapping("/cells")
    public Map<String, String> getCells(@RequestParam int x1, @RequestParam int y1,
                                        @RequestParam int x2, @RequestParam int y2) {
        return redisService.getCellsInArea(x1, y1, x2, y2);
    }

    @PostMapping("/cell/{x}/{y}")
    public Mono<Void> updateCell(@PathVariable("x") int x, @PathVariable("y") int y, @RequestBody(required = false) String color) {
        Cell cell = new Cell();
        cell.setX(x);
        cell.setY(y);
        cell.setColor(color);

        redisService.saveCell(cell);
        
        String message = String.format("{\"type\":\"cellUpdate\",\"x\":%d,\"y\":%d,\"color\":\"%s\"}", x, y, color);
        websocketHandler.broadcastCellUpdate(message);
        
        return Mono.empty();
    }
}