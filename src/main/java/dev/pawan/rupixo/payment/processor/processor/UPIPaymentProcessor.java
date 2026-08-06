package dev.pawan.rupixo.payment.processor.processor;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.payment.processor.PaymentProcessor;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorRequest;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UPIPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String VPA_CODE_FAIL = "fail@oksbi";

        String vpa = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        if(VPA_CODE_FAIL.equals(vpa)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Bank Rejected the transaction registration");
        }

        // mock data
        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
