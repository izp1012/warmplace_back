package com.warmplace.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String username, String token, long expirationMillis) {
        String key = "jwt:token:" + username;
        redisTemplate.opsForValue().set(key, token, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getToken(String username) {
        String key = "jwt:token:" + username;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteToken(String username) {
        String key = "jwt:token:" + username;
        redisTemplate.delete(key);
    }

    public boolean hasToken(String username) {
        String key = "jwt:token:" + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
