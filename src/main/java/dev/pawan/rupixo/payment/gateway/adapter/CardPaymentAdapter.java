package dev.pawan.rupixo.payment.gateway.adapter;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.gateway.PaymentAdapter;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
import dev.pawan.rupixo.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        String token = (String) paymentRequest.methodDetails().get("token");

        PaymentProcessorResponse paymentProcessorResponse = vaultService.charge(
                paymentRequest.paymentId(),
                token,
                paymentRequest.amount(),
                paymentRequest.methodDetails()
        );
        return switch (paymentProcessorResponse) {
            case PaymentProcessorResponse.Failure failure ->
                    new PaymentResult.Failed(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankRef());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorRef());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF_" + RandomizerUtil.randomBase64(16));
    }
}
