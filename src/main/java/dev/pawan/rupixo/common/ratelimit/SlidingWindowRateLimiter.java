package dev.pawan.rupixo.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limiter.strategy", havingValue = "sliding-window")
public class SlidingWindowRateLimiter implements RateLimiter {

    public static final String REDIS_KEY_PREFIX = "rate-limit:sliding-window:";
    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult checkRateLimit(String key, long maxRequestsAllowed, long timeWindowInSeconds) {
        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - (timeWindowInSeconds * 1000);

        String redisKey = REDIS_KEY_PREFIX + key;

        ZSetOperations<String, String> zset = redis.opsForZSet();
        zset.removeRangeByScore(redisKey, Double.NEGATIVE_INFINITY, floorMs);

        Long count = zset.zCard(redisKey);
        long current = count != null ? count : 0;

        if (current >= maxRequestsAllowed) {
            var oldestEntry = zset.rangeWithScores(redisKey, 0, 0)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (oldestEntry != null) {
                Double oldestTimeStamp = oldestEntry.getScore();
                long retryAfter = 1;
                if (oldestTimeStamp != null) {
                    long windowExpiresMs = oldestTimeStamp.longValue() + timeWindowInSeconds * 1000;
                    retryAfter = windowExpiresMs - nowMs;
                }
                return RateLimitResult.denied(retryAfter);
            }
        }
        zset.add(redisKey, UUID.randomUUID().toString(), nowMs);
        redis.expire(redisKey, timeWindowInSeconds, TimeUnit.SECONDS);
        return RateLimitResult.allowed(maxRequestsAllowed - current - 1);
    }
}
