package dev.pawan.rupixo.payment.statemachine;

import dev.pawan.rupixo.common.enums.PaymentActor;
import dev.pawan.rupixo.common.enums.PaymentEvent;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.merchant.security.MerchantContext;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.entity.PaymentTransitionLog;
import dev.pawan.rupixo.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTransitionLogService {
    private final PaymentStateMachine paymentStateMachine;
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final ObjectProvider<MerchantContext> merchantContextProvider;

    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent) {
        PaymentStatus next = paymentStateMachine.transistion(payment.getStatus(), paymentEvent);

        PaymentActor actor = getPaymentActor();

        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .event(paymentEvent)
                .payment(payment)
                .fromStatus(payment.getStatus())
                .toStatus(next)
                .actor(actor)
                .occurredAt(LocalDateTime.now())
                .build();
        payment.setStatus(next);
        paymentTransitionLogRepository.save(paymentTransitionLog);

        return next;
    }

    private PaymentActor getPaymentActor() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return PaymentActor.SYSTEM; // scheduler, async, startup, etc.
        }
        MerchantContext ctx = merchantContextProvider.getObject();
        return ctx.getMerchantId() != null ? PaymentActor.MERCHANT : PaymentActor.CUSTOMER;
    }
}
