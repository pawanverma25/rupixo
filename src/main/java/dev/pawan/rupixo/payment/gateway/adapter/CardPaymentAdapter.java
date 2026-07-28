package dev.pawan.rupixo.payment.gateway.adapter;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.gateway.PaymentAdapter;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
