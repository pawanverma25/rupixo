package dev.pawan.rupixo.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failed, PaymentResult.Pending, PaymentResult.Success{
    record Pending(String paymentRegistrationRef) implements PaymentResult {}
    record Failed(String errorCode, String errorDescription) implements PaymentResult {}
    record Success(String bankReference) implements PaymentResult {}
}
