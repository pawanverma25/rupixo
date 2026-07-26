package dev.pawan.rupixo.payment.processor;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorRequest;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {
    PaymentMethod getPaymentMethod();
    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
