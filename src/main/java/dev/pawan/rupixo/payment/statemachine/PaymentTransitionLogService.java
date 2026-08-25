package dev.pawan.rupixo.payment.statemachine;

import dev.pawan.rupixo.common.enums.PaymentActor;
import dev.pawan.rupixo.common.enums.PaymentEvent;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.entity.PaymentTransitionLog;
import dev.pawan.rupixo.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTransitionLogService {
    private final PaymentStateMachine paymentStateMachine;
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;

    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){
        PaymentStatus next = paymentStateMachine.transistion(payment.getStatus(), paymentEvent);

        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .event(paymentEvent)
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO: fetch data from merchant context
                .occurredAt(LocalDateTime.now())
                .build();
        payment.setStatus(next);
        paymentTransitionLogRepository.save(paymentTransitionLog);

        return next;
    }
}
