package dev.pawan.rupixo.operations.webhook;

import dev.pawan.rupixo.common.enums.WebhookEventStatus;
import dev.pawan.rupixo.operations.entity.DlqEvent;
import dev.pawan.rupixo.operations.entity.WebhookEvent;
import dev.pawan.rupixo.operations.repository.DlqEventRepository;
import dev.pawan.rupixo.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqEventRecorder {
    private final DlqEventRepository dlqEventRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWebhookEventAfterExhaustion(WebhookEvent webhookEvent, String finalError){
        webhookEvent.setStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(webhookEvent);

        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(webhookEvent)
                .merchantId(webhookEvent.getMerchantId())
                .movedAt(LocalDateTime.now())
                .finalError(finalError)
                .payload(webhookEvent.getPayload())
                .build();
        dlqEventRepository.save(dlqEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public  void recordAfterConsumerFailure(ConsumerRecord<String, Map<String, Object>> consumerRecord, String errorMessage) {
        Map<String, Object> envelope = consumerRecord.value();

        UUID merchantId = null;
        try{
            Map<String, Object> data = (Map<String, Object>) envelope.get("payload");
            Object merchantIdObj = data.get("merchantId");
            if (merchantIdObj != null) {
                merchantId = UUID.fromString((String) merchantIdObj);
            }
        } catch (Exception e){
            log.error("Failed to extract merchantId from consumer record: {}", consumerRecord, e);
        }

        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(null)
                .merchantId(merchantId)
                .movedAt(LocalDateTime.now())
                .finalError(errorMessage)
                .payload(envelope != null ? envelope : Map.of())
                .build();
        dlqEventRepository.save(dlqEvent);
    }

}
