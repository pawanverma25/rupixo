package dev.pawan.rupixo.payment.outbox;

import dev.pawan.rupixo.common.enums.OutboxStatus;
import dev.pawan.rupixo.payment.entity.OutboxEvent;
import dev.pawan.rupixo.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxEventHandler {

    private final OutboxEventRepository outboxEventRepository;
    public static final int MAX_ATTEMPTS = 3;

    @Transactional
    public void handleSuccessfulEvent(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        outboxEventRepository.save(event);
    }

    @Transactional
    public void handleFailedEvent(OutboxEvent event, String errorMessage) {
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(errorMessage.substring(0, Math.min(errorMessage.length(), 1000)));
        if(event.getAttempts() >= MAX_ATTEMPTS) {
            event.setStatus(OutboxStatus.FAILED);
        }
        outboxEventRepository.save(event);
    }
}
