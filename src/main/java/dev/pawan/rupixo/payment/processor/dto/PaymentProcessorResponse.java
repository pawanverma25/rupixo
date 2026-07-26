package dev.pawan.rupixo.payment.processor.dto;

public sealed interface PaymentProcessorResponse
        permits PaymentProcessorResponse.Pending, PaymentProcessorResponse.Failure, PaymentProcessorResponse.Success{

    record Pending(String processorRef) implements PaymentProcessorResponse {}
    record Success(String processorRef, String bankRef) implements PaymentProcessorResponse{}
    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponse{}

}
