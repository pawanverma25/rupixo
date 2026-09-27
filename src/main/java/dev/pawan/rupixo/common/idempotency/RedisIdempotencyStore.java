package dev.pawan.rupixo.common.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try {
            Boolean set = redis.opsForValue().setIfAbsent(IDEMPOTENCY_KEY_PREFIX + key, IN_PROGRESS, ttl);
            return Boolean.TRUE.equals(set);
        } catch (Exception e) {
            log.error("Idempotency Store is unavailable, failing open for key: {}", key, e);
        }
        return true;
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try {
            redis.opsForValue().set(IDEMPOTENCY_KEY_PREFIX + key, value, ttl);
        } catch (Exception e) {
            log.error("Error occurred while setting idempotency key: {}", key, e);
        }

    }

    @Override
    public Optional<String> get(String key) {
        try {
            String value = redis.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Error occurred while getting idempotency key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redis.delete(IDEMPOTENCY_KEY_PREFIX + key);
        } catch (Exception e) {
            log.error("Error occurred while deleting idempotency key: {}", key, e);
        }
    }
}
