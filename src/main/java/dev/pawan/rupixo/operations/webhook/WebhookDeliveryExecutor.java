package dev.pawan.rupixo.operations.webhook;

import dev.pawan.rupixo.common.enums.WebhookEventStatus;
import dev.pawan.rupixo.operations.entity.WebhookEvent;
import dev.pawan.rupixo.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryExecutor {

    private final WebhookEventRepository webhookEventRepository;
    private  final WebhookRetryQueue webhookRetryQueue;
    private final RestClient restClient;

    @Value("${app.webhook.delivery.client.signature-header:X-Rupixo-Signature}")
    private final String REST_CLIENT_SIGNATURE_HEADER;

    private final List<Duration> BACKOFF = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            Duration.ofHours(2),
            Duration.ofHours(8),
            Duration.ofHours(24)
    );

    private static final int MAX_RETRIES = 7;

    public void deliverWebhook(UUID webhookEventId) {
        Optional<WebhookEvent> webhookEventOpt = webhookEventRepository.findById(webhookEventId);
        if (webhookEventOpt.isEmpty()) {
            log.warn("Webhook event with ID {} not found", webhookEventId);
            return;
        }

        var event = webhookEventOpt.get();

        event.setAttempts(event.getAttempts() + 1);
        event.setLastAttemptAt(LocalDateTime.now());

        try {

            var response = restClient.post()
                    .uri(event.getTargetUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(REST_CLIENT_SIGNATURE_HEADER, event.getSignature())
                    .body(Map.of(
                            "eventType", event.getEventType(),
                            "data", event.getPayload()
                    ))
                    .retrieve()
                    .toBodilessEntity();

            event.setLastResponseCode(response.getStatusCode().value());

            if(response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(WebhookEventStatus.DELIVERED);
                event.setDeliveredAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                return;
            }

            handleAttemptFailed(event, "HTTP " + response.getStatusCode());

        } catch (RestClientException e) {
            log.error("Failed to deliver webhook event with ID {}", webhookEventId, e);
            handleAttemptFailed(event, e.getMessage());
        }
    }

    private void handleAttemptFailed(WebhookEvent event, String error) {
        event.setLastResponseBody(error);

        if(event.getAttempts() >= MAX_RETRIES) {
            event.setStatus(WebhookEventStatus.DEAD);
            log.warn("Max retries reached for webhook event with ID {}. Marking as dead.", event.getId());

            //TODO: Add event to to DLQ

            return;
        }

        event.setNextRetryAt(LocalDateTime.now().plus(BACKOFF.get(event.getAttempts() - 1)));
        event.setStatus(WebhookEventStatus.FAILED);

        webhookEventRepository.save(event);

        webhookRetryQueue.enqueue(event.getId(), event.getNextRetryAt());
    }
}
