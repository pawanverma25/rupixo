package dev.pawan.rupixo.payment.gateway;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayAdapterRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    public PaymentResult initiate(PaymentRequest request){
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(request.method());
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No payment adapter registered to for method: " + request.method());
        }
        return paymentAdapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(method);
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No payment adapter registered to for method: " + method);
        }
        return paymentAdapter.capture(paymentId);
    }
}
