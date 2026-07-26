package dev.pawan.rupixo.payment.processor.dto;

import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.common.enums.PaymentMethod;

import java.util.Map;

public record PaymentProcessorRequest(
        PaymentMethod paymentMethod,
        Money amount,
        Map<String, Object> methodDetails
) {
}
