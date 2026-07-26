package dev.pawan.rupixo.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failed, PaymentResult.Pending{
    record Pending(String paymentRegistrationRef) implements PaymentResult {}
    record Failed(String errorCode, String errorDescription) implements PaymentResult {}
}
