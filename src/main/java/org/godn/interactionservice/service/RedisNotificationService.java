package org.godn.interactionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RedisNotificationService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisNotificationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void handleBotNotification(String postAuthorId, String botId) {
        String cooldownKey = "user:" + postAuthorId + ":notif_cooldown";
        String pendingQueueKey = "user:" + postAuthorId + ":pending_notifs";
        String message = "Bot " + botId + " replied to your post";

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            stringRedisTemplate.opsForList().leftPush(pendingQueueKey, message);
        } else {
            log.info("Push Notification Sent to User: {}", message);
            stringRedisTemplate.opsForValue().set(cooldownKey, "locked", Duration.ofMinutes(15));
        }
    }
}
