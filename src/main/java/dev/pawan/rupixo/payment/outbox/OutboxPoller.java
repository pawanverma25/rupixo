package dev.pawan.rupixo.payment.outbox;

import dev.pawan.rupixo.common.config.KafkaProperties;
import dev.pawan.rupixo.common.enums.OutboxStatus;
import dev.pawan.rupixo.payment.entity.OutboxEvent;
import dev.pawan.rupixo.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxEventHandler outboxEventHandler;

    //TODO: schedule this method to run periodically using @Scheduled annotation
    @Scheduled(fixedDelayString = "${app.kafka.outbox.poll-interval-ms:5000}")
    public void poll(){
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {
            String topic = kafkaProperties.topicFor(event.getAggregateType());
            String key = extractMerchantIdFromPayload(event.getPayload());
            try {
                Map<String, Object> envelopedPayload = Map.of(
                        "eventId", event.getId().toString(),
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType().name(),
                        "aggregateId", event.getAggregateId().toString(),
                        "payload", event.getPayload()
                );

                kafkaTemplate.send(topic, key, envelopedPayload).get(5, TimeUnit.SECONDS);

                log.info("Successfully sent outbox event with id: {}", event.getId());
                outboxEventHandler.handleSuccessfulEvent(event);

            } catch (Exception e) {
                log.error("Failed to send outbox event with id: {}, attempts: {}", event.getId(), event.getAttempts(), e);
                outboxEventHandler.handleFailedEvent(event, e.getMessage());
            }
        }
    }

    private String extractMerchantIdFromPayload(Map<String, Object> payload) {
        Object key = payload.get("merchantId");
        return key != null ? key.toString() : "unknown";
    }
}
