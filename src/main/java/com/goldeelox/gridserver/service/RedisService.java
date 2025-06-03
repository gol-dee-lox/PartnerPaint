package com.goldeelox.gridserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldeelox.gridserver.model.Cell;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class RedisService {

    private JedisPooled jedis;

    public RedisService() {
    	jedis=new JedisPooled("");
        // Read from your Render env vars:
        String redisUrl = System.getenv("REDIS_URL");
        String redisToken = System.getenv("REDIS_TOKEN");

        if (redisUrl == null || redisToken == null) {
            throw new IllegalStateException("Missing Redis env vars");
        }

        URI uri;
		try {
			uri = new URI(redisUrl);
			this.jedis = new JedisPooled(uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    public void saveCell(Cell cell) {
        String key = getKey(cell.getX(), cell.getY());
        jedis.set(key, cell.getColor());
        System.out.println("Redis saved: " + key + " -> " + cell.getColor());
    }

 // add ObjectMapper once per class
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> getCellsInArea(int x1, int y1, int x2, int y2) {
        List<String> keys = new ArrayList<>();
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                keys.add(getKey(x, y));
            }
        }

        List<String> values = jedis.mget(keys.toArray(new String[0]));

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String value = values.get(i);
            if (value != null) {
                try {
                    // Parse JSON string into Map
                    Map<String, String> parsed = objectMapper.readValue(value, Map.class);
                    String color = parsed.get("color");
                    result.put(formatKey(keys.get(i)), color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Redis returned: " + result);
        return result;
    }

    private String getKey(int x, int y) {
        return "cell:" + x + ":" + y;
    }

    private String formatKey(String fullKey) {
        return fullKey.replace("cell:", "").replace(":", ",");
    }
}