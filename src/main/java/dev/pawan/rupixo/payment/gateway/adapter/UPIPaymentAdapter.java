package dev.pawan.rupixo.payment.gateway.adapter;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.gateway.PaymentAdapter;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import dev.pawan.rupixo.payment.processor.PaymentProcessorRouter;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorRequest;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UPIPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("Initiate Payment with UPIPaymentAdapter, paymentId: {}", paymentRequest.paymentId());

        try{
            PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest.NonCard(
                    paymentRequest.paymentId(),
                    getPaymentMethod(),
                    paymentRequest.amount(),
                    paymentRequest.methodDetails()
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse){
                case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failed(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorRef());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankRef());
            };
        } catch (Exception e) {
            log.warn("UPIPaymentAdapter Failed, paymentId: {}", paymentRequest.paymentId());
            return new PaymentResult.Failed("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_SUCCESS");
    }
}
