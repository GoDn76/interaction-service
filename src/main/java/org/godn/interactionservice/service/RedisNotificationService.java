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

    private static final String ACTIVE_USERS_SET = "active:pending_notif_users";


    public RedisNotificationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void handleBotNotification(String postAuthorId, String botId, Duration cooldown) {

        String cooldownKey = "user:" + postAuthorId + ":notif_cooldown";
        String pendingQueueKey = "user:" + postAuthorId + ":pending_notifs";
        String message = "Bot " + botId + " replied to your post";

        Boolean lockAcquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "locked", cooldown);

        if (Boolean.TRUE.equals(lockAcquired)) {
            log.info("Push Notification Sent to User: {}", message);
        } else {
            stringRedisTemplate.opsForList().leftPush(pendingQueueKey, message);
            stringRedisTemplate.opsForSet().add(ACTIVE_USERS_SET, postAuthorId);
        }
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void sweepPendingNotifications() {

        Set<String> users = stringRedisTemplate.opsForSet().members(ACTIVE_USERS_SET);

        if (users == null || users.isEmpty()) {
            return;
        }

        log.info("CRON Sweeper started. Checking pending notifications for {} users.", users.size());

        for (String userId : users) {

            String queueKey = "user:" + userId + ":pending_notifs";
            List<String> messages = stringRedisTemplate.opsForList().range(queueKey, 0, -1);

            if (messages != null && !messages.isEmpty()) {

                int totalPending = messages.size();
                String firstMessage = messages.get(0);

                String firstBotId = firstMessage.split(" ")[1];
                int othersCount = totalPending - 1;

                if (othersCount > 0) {
                    log.info("Summarized Push Notification: Bot {} and {} others interacted with your posts.",
                            firstBotId, othersCount);
                } else {
                    log.info("Summarized Push Notification: {}", firstMessage);
                }

                stringRedisTemplate.delete(queueKey);
            }

            stringRedisTemplate.opsForSet().remove(ACTIVE_USERS_SET, userId);
        }

        log.info("Sweeping complete.");
    }
}
