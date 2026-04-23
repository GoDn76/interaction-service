# interaction-service
Interaction Service Assessment

# Phase 1: Core API & Database Setup
### DB Design Choices
- **Didn't used relationships for authorId since it required to be either User or Bot.**
  - **Why** - It is given that **authorId** can either be User or Bot, so if we map a strict foreign key to either table it'll crash when other type tries to access it.

  
### Service Assumption
- **Assumption made** - The assignment doesn't specify a Virality Score for "Bot like". Therefore, I have assumed that there will be no Increment made on the action of a "Bot Like" only Interaction will be processed. **(Redis Increment will be skipped, treating it as +0 points)**

# Phase 2 – Thread Safety & Atomic Locks

To handle concurrency safely, I used Redis as a gatekeeper before committing anything to the database.  
The application itself does not store any counters or cooldowns in memory, which keeps it stateless.

---

## Horizontal Cap (Max 100 Bot Replies per Post)

For each post, I maintain a Redis key:

post:{postId}:bot_count

When a bot tries to comment:

1. I increment the counter using Redis `INCR`.
2. If the value becomes greater than 100, I immediately decrement it and reject the request with `429 Too Many Requests`.
3. The comment is saved to the database only if the counter check passes.

Since `INCR` in Redis is atomic, even if 200 concurrent requests hit at the same time, Redis processes them safely and only allows the first 100 to pass.

This ensures the database never ends up with more than 100 bot comments for a post.

**ISSUES ENCOUNTERED** - Not a major issue bit it was instructed that Redis Horizontal Cap should be stopped perfectly at 100, but since Redis `INCR` increments first and then checks the value, instead capping at 100 it showed all the requests intercepted by redis. To solve this, I wrote a custom LUA script in RedisInteractionService.

---

## Cooldown Cap (Bot ↔ Human Interaction)

To prevent a bot from interacting with the same human more than once in 10 minutes, I use:

cooldown:bot_{botId}:human_{humanId}

I set this key using `SET NX` or `setIfAbsent` in java with a 10-minute TTL.

- If the key already exists → the request is rejected.
- If it does not exist → the key is created and the interaction proceeds.

Since `SET NX` is atomic in Redis, there is no race condition even if multiple requests happen simultaneously.

---

## Vertical Cap (Thread Depth Limit)

Before saving a reply, I calculate its depth based on the parent comment.

If the depth exceeds 20 levels, the request is rejected.

This validation is done before saving to the database.

---

## Statelessness

The application does not use:

- Static variables
- In-memory counters
- Synchronized blocks

All shared state (bot counts, cooldowns, notification queues) is stored in Redis.

This ensures the service can scale horizontally without losing correctness.

---

## Database Integrity

Redis is always checked before saving to PostgreSQL.

If any guardrail fails:
- An exception is thrown
- The transaction rolls back
- No invalid data is committed.

# Phase 3 – Notification Engine (Smart Batching)

The goal of Phase 3 was to prevent notification spam when bots interact with user posts.  
Instead of sending a push notification immediately for every bot action, notifications are batched using Redis.

---

## Notification Flow

When a bot interacts with a user’s post:

1. The system checks if the user has received a notification in the last 15 minutes.
2. If the user is NOT on cooldown:
   - A push notification is logged immediately.
   - A cooldown key is created in Redis with a 15-minute TTL.
3. If the user IS on cooldown:
   - The notification message is added to a Redis List for that user.
   - The user ID is added to a Redis Set that tracks users with pending notifications.

---

## Redis Keys Used

Cooldown key: `user:{userId}:notif_cooldown`

Pending notifications list: `user:{userId}:pending_notifs`

Active users set (for sweeping): `active:pending_notif_users`

---

## Why a Set is Used

Instead of scanning Redis keys using `KEYS`, I maintain a separate Redis Set that stores user IDs who currently have pending notifications.

This avoids scanning the entire keyspace and makes the sweeper more efficient and scalable.

---

## CRON Sweeper

A scheduled task runs every 5 minutes.

For each user in the active users set:

1. All messages are drained from their pending notification list.
2. A summarized message is logged in the format:

   "Bot X and N others interacted with your posts."

3. The user is removed from the active users set once their queue is empty.

To avoid race conditions, the list is drained using repeated `RPOP` operations instead of reading and deleting the entire list at once.

This ensures that if a new notification arrives during the sweep, it will not be lost and will be processed in the next cycle.

---

## Statelessness

All notification state is stored in Redis:

- Cooldown tracking
- Pending notification queues
- Active users tracking

No in-memory structures are used, which keeps the service stateless and safe for horizontal scaling.