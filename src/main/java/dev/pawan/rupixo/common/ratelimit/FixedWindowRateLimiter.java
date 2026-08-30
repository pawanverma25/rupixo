package dev.pawan.rupixo.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limiter.strategy", havingValue = "fixed-window")
public class FixedWindowRateLimiter implements RateLimiter{

    public static final String REDIS_KEY_PREFIX = "rate-limit:fixed-window:";
    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult checkRateLimit(String key, long maxRequestsAllowed, long timeWindowInSeconds) {
        String redisKey = REDIS_KEY_PREFIX + key;

        Long currentCount = redis.opsForValue().increment(redisKey);
        if(currentCount == null) return RateLimitResult.allowed(maxRequestsAllowed);

        if(currentCount == 1) {
            redis.expire(redisKey, timeWindowInSeconds, TimeUnit.SECONDS);
        }

        if(currentCount > maxRequestsAllowed) {
            Long ttl = redis.getExpire(redisKey, TimeUnit.SECONDS);
            long timeUntilReset = ttl != null && ttl > 0 ? ttl : timeWindowInSeconds;
            return RateLimitResult.denied(timeUntilReset);
        }

        return RateLimitResult.allowed(maxRequestsAllowed - currentCount);
    }
}
