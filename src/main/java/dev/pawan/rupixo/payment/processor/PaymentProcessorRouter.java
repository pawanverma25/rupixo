package dev.pawan.rupixo.payment.processor;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorRequest;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest paymentProcessorRequest){
        PaymentProcessor paymentProcessor = paymentProcessorMap.get(paymentProcessorRequest.paymentMethod());
        if(paymentProcessor == null){
            throw new IllegalArgumentException("No Payment processor is registered for method: " + paymentProcessorRequest.paymentMethod());
        }

        return paymentProcessor.charge(paymentProcessorRequest);
    }
}
