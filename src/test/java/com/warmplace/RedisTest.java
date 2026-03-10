package com.warmplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testConnection() {
        redisTemplate.opsForValue().set("testKey", "Hello Upstash!");
        String value = redisTemplate.opsForValue().get("testKey");
        System.out.println("Result: " + value);
    }
}