package org.godn.interactionservice.service;

import org.godn.interactionservice.exception.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

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
            throw new RateLimitExceededException("Cooldown active: Bot cannot interact with this human yet.");
        }
    }

    public void limitBotInteraction(String postId, Integer limit) {
        String key = "post:" + postId + ":bot_count";

        String script = """
        local current = redis.call("GET", KEYS[1])
        if not current then
            current = 0
        else
            current = tonumber(current)
        end
        if current < tonumber(ARGV[1]) then
            return redis.call("INCR", KEYS[1])
        else
            return -1
        end
    """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, List.of(key), String.valueOf(limit));

        if (result == null) {
            throw new RuntimeException("Redis execution failed: Unable to verify bot cap.");
        }

        if (result == -1L) {
            throw new RateLimitExceededException("Too Many Requests: Horizontal cap reached.");
        }
    }

}
