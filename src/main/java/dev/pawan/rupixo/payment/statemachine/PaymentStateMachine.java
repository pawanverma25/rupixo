package dev.pawan.rupixo.payment.statemachine;

import dev.pawan.rupixo.common.enums.PaymentEvent;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.common.exception.InvalidStateTransitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentStateMachine {
    public record Transition(
            PaymentStatus fromStatus,
            PaymentEvent event
    ){};

    public static final Map<Transition, PaymentStatus> TRANSITION = Map.ofEntries(
            Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.AUTHORIZE_ATTEMPT), PaymentStatus.AUTHORIZING),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_SUCCESS), PaymentStatus.AUTHORIZED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.AUTHORIZE_FAIL), PaymentStatus.FAILED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CAPTURE_REQUEST), PaymentStatus.CAPTURING),
            Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_SUCCESS), PaymentStatus.CAPTURED),
            Map.entry(new Transition(PaymentStatus.CAPTURING, PaymentEvent.CAPTURE_FAIL), PaymentStatus.FAILED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.SETTLE), PaymentStatus.SETTLED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.SETTLED, PaymentEvent.REFUND_INIT), PaymentStatus.PARTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.PARTIALLY_REFUNDED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CAPTURED, PaymentEvent.REFUND_COMPLETE), PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CREATED, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING, PaymentEvent.CANCEL), PaymentStatus.CANCELLED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED, PaymentEvent.CANCEL), PaymentStatus.CANCELLED)
    );

    public PaymentStatus transistion(PaymentStatus from, PaymentEvent event){
        PaymentStatus nextStatus = TRANSITION.get(new Transition(from, event));
        if(nextStatus == null){
            throw new InvalidStateTransitionException(from.name(), event.name());
        }

        return nextStatus;
    }
}
