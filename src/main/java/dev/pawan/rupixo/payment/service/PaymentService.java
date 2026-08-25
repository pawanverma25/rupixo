package dev.pawan.rupixo.payment.service;

import dev.pawan.rupixo.payment.dto.request.PaymentInitRequest;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest paymentInitRequest);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean isSuccessful, String bankRef, String errorCode, String errorMessage);
}
