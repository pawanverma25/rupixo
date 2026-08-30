package dev.pawan.rupixo.common.ratelimit;

public record RateLimitResult (
        boolean isAllowed,
        long remainingRequests,
        long resetTimeMillis
){
    public static RateLimitResult allowed(long remainingRequests) {
        return new RateLimitResult(true, remainingRequests, 0);
    }
    public static RateLimitResult denied(long resetTimeMillis) {
        return new RateLimitResult(false, 0, resetTimeMillis);
    }
}
