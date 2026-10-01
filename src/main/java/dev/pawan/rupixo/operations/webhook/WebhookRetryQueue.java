package dev.pawan.rupixo.operations.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebhookRetryQueue {
    private final StringRedisTemplate redis;

    @Value("${app.webhook.delivery.redis-key:webhook-retry}")
    private final String key;

    public void enqueue(UUID webhookEventId, LocalDateTime retryAt){
        long time = getTime(retryAt);
        redis.opsForZSet().add(
                key,
                webhookEventId.toString(),
                time
        );
    }

    public Set<UUID> pollDue(int batchSize){
        long now = getTime(LocalDateTime.now());
        var dues = redis.opsForZSet().rangeByScoreWithScores(key, 0, now, 0, batchSize);
        if(dues == null || dues.isEmpty()){
            return Set.of();
        }

        dues.forEach(entry -> redis.opsForZSet().remove(key, entry.getValue()));

        return dues.stream().map(entry -> UUID.fromString(entry.getValue()))
                .collect(Collectors.toSet());
    }

    public void enqueueIfAbsent(UUID id, LocalDateTime nextRetryAt) {
        long nextRetryAtTime = getTime(nextRetryAt);
        redis.opsForZSet().addIfAbsent(key, id.toString(), nextRetryAtTime);
    }

    private static long getTime(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
