package dev.pawan.rupixo.common.ratelimit;

public interface RateLimiter {
    RateLimitResult checkRateLimit(String key, long maxRequestsAllowed, long timeWindowInSeconds);
}
