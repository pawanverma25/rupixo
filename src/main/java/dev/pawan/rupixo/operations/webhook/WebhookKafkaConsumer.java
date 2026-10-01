package dev.pawan.rupixo.operations.webhook;

import dev.pawan.rupixo.common.dto.WebhookTarget;
import dev.pawan.rupixo.common.enums.WebhookEventStatus;
import dev.pawan.rupixo.common.util.SignerUtil;
import dev.pawan.rupixo.merchant.api.MerchantWebhookApi;
import dev.pawan.rupixo.operations.entity.WebhookEvent;
import dev.pawan.rupixo.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookKafkaConsumer {

    private final MerchantWebhookApi merchantWebhookApi;
    private final ObjectMapper objectMapper;
    private final WebhookEventRepository webhookEventRepository;
    private final SignerUtil signerUtil;
    private final WebhookRetryQueue webhookRetryQueue;

    @KafkaListener(topics = {
            "${app.kafka.topics.payments:payments.events}",
            "${app.kafka.topics.orders:orders.events}",
            "${app.kafka.topics.refunds:refunds.events}",
            "${app.kafka.topics.settlements:settlements.events}"
    })
    public void onWebhookEvent(ConsumerRecord<String, Map<String, Object>> consumerRecord, Acknowledgment ack) {
        try {
            Map<String, Object> envelope = consumerRecord.value();
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            String eventType = (String) envelope.get("eventType");
            Object merchantIdObj = data.get("merchantId");

            if (merchantIdObj == null) {
                log.warn("Cannot process the webhook even as merchantId is null for eventType {}", eventType);
                ack.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString((String) merchantIdObj);

            List<WebhookTarget> targets = merchantWebhookApi.getActiveConfigByEventType(merchantId, eventType);
            if (targets.isEmpty()) {
                log.debug("No webhook target was found, skipping event: {}", eventType);
                ack.acknowledge();
                return;
            }


            Map<String, Object> signatureData = Map.of("event", eventType, "payload", data);
            String signatureJson = objectMapper.writeValueAsString(signatureData);

            for (WebhookTarget target : targets) {
                String signature = signerUtil.sign(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEvent = webhookEventRepository.save(webhookEvent);
                log.info("Webhook event created for merchantId={} eventType={} targetUrl={} webhookEventId={}",
                        merchantId, eventType, target.targetUrl(), webhookEvent.getId());

                webhookRetryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing webhook event on offset={}", consumerRecord.offset(), e);
            //TODO: Decide whether to ack or not. If we don't ack, the message will be retried, which may lead to duplicate webhook events.
        }
    }
}
