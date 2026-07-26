package dev.pawan.rupixo.payment.processor.dto;

import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public sealed interface PaymentProcessorRequest permits PaymentProcessorRequest.Card, PaymentProcessorRequest.NonCard {
    PaymentMethod paymentMethod();
    UUID processingId();
    UUID paymentId();
    Money amount();
    Map<String, Object> methodDetails();
    record Card(
            UUID processingId,
            UUID paymentId,
            PaymentMethod paymentMethod,
            Money amount,
            String pan,
            String expiry,
            Map<String, Object> methodDetails
    )  implements PaymentProcessorRequest {}

    record NonCard(
            UUID processingId,
            UUID paymentId,
            PaymentMethod paymentMethod,
            Money amount,
            Map<String, Object> methodDetails
    ) implements PaymentProcessorRequest{}
}
