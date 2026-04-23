package org.godn.interactionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RedisNotificationService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisNotificationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void handleBotNotification(String postAuthorId, String botId, Duration cooldown) {
        String cooldownKey = "user:" + postAuthorId + ":notif_cooldown";
        String pendingQueueKey = "user:" + postAuthorId + ":pending_notifs";
        String message = "Bot " + botId + " replied to your post";

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            stringRedisTemplate.opsForList().leftPush(pendingQueueKey, message);
        } else {
            log.info("Push Notification Sent to User: {}", message);
            stringRedisTemplate.opsForValue().set(cooldownKey, "locked", cooldown);
        }
    }

    @Scheduled(fixedRate = 300000) // For 5 minutes...
    public void sweepPendingNotifications() {
        Set<String> queueKeys = stringRedisTemplate.keys("user:*:pending_notifs");

        if(queueKeys == null || queueKeys.isEmpty()) {
            return;
        }

        log.info("CRON Sweeper started. Checking pending notifications for {} users.", queueKeys.size());

        for (String queueKey : queueKeys) {
            List<String> messages = stringRedisTemplate.opsForList().range(queueKey, 0, -1); // Getting all the messages...

            if (messages != null && !messages.isEmpty()) {
                int totalPending = messages.size();
                String firstMessage = messages.get(0);

                String firstBotId = firstMessage.split(" ")[1];
                int othersCount = totalPending - 1;

                if (othersCount > 0) {
                    log.info("Summarized Push Notification: Bot {} and {} others interacted with your posts.", firstBotId, othersCount);
                } else {
                    log.info("Summarized Push Notification: {}", firstMessage);
                }

                // Remove from Redis...
                stringRedisTemplate.delete(queueKey);
            }
        }
        log.info("Sweeping complete.");

    }
}
