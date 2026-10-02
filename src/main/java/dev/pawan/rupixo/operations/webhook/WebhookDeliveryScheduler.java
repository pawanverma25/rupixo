package dev.pawan.rupixo.operations.webhook;

import dev.pawan.rupixo.common.enums.WebhookEventStatus;
import dev.pawan.rupixo.operations.entity.WebhookEvent;
import dev.pawan.rupixo.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryScheduler {

    private final WebhookRetryQueue webhookRetryQueue;
    private final WebhookEventRepository webhookEventRepository;
    private final ExecutorService virtualThreadExecutorService;
    private final WebhookDeliveryExecutor webhookDeliveryExecutor;

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize = 100;


    @Scheduled(fixedDelayString = "${app.webhook.delivery.poll-interval-ms:1000}")
    public void pollAndDeliver() {
        Set<UUID> dues = webhookRetryQueue.pollDue(batchSize);
        if(dues.isEmpty()){
            log.debug("No due webhook events found for delivery");
            return;
        }

        for(UUID due : dues) {
            virtualThreadExecutorService.submit(() -> {
               webhookDeliveryExecutor.deliverWebhook(due);
           });
        }
    }

    @Scheduled(fixedDelayString = "${app.webhook.delivery.reconcile-interval-ms:60000}")
    public void reconcileFromDB(){
        LocalDateTime now = LocalDateTime.now();
        List<WebhookEvent> dueEvents = webhookEventRepository.findByStatusAndNextRetryAtBefore(
                WebhookEventStatus.PENDING,
                now
        );

        for(WebhookEvent event : dueEvents){
            webhookRetryQueue.enqueueIfAbsent(event.getId(), event.getNextRetryAt());
        }

    }
}
