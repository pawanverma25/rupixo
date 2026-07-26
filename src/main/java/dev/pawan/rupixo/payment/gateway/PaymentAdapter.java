package dev.pawan.rupixo.payment.gateway;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;

public interface PaymentAdapter {
    public PaymentMethod getPaymentMethod();
    public PaymentResult initiate(PaymentRequest paymentRequest);
}
