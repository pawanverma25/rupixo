package dev.pawan.rupixo.common.idempotency;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {
    String IDEMPOTENCY_KEY_PREFIX = "idempotency-key:";
    String IN_PROGRESS = "__IN_PROGRESS__";

    boolean setIfAbsent(String key, Duration ttl);

    void store(String key, String value, Duration ttl);

    Optional<String> get(String key);

    void delete(String key);
}
