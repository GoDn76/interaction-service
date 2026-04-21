package org.godn.interactionservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisInteractionService {
    private final StringRedisTemplate redisTemplate;

    public RedisInteractionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void increaseScore(String postId, Long score) {
        String redisKey = "post:"+postId+":virality_score";
        redisTemplate.opsForValue().increment(redisKey, score);
    }

    public void botInteractionCooldown(String botId, String userId, Duration cooldown) {
        String redisKey = "cooldown:bot_"+botId+":human_"+userId;

        Boolean isAllowed = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "locked", cooldown);

        if (Boolean.FALSE.equals(isAllowed)) {
            throw new RuntimeException("Cooldown active: Bot cannot interact with this human yet.");
        }
    }

    public void limitBotInteraction(String postId, Integer limit) {
        String botCountKey = "post:"+postId+":bot_count";

        Long newCount = redisTemplate.opsForValue().increment(botCountKey);

        if (newCount != null && newCount > limit) {
            throw new RuntimeException("Horizontal cap reached.");
        }
    }

}
