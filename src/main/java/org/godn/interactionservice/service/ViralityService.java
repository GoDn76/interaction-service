package org.godn.interactionservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViralityService {
    private final StringRedisTemplate redisTemplate;

    public ViralityService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void IncreaseScore(String postId, Long score) {
        String redisKey = "post"+postId+"virality_score";
        redisTemplate.opsForValue().increment(redisKey, score);
    }
}
