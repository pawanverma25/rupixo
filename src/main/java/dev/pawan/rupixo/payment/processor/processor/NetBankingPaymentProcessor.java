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
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.NETBANKING;
    }

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("BANK").toString() : null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Bank Rejected the transaction registration");
        }

        // mock data
        String processorRef = "NBK_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        String redirectRef = "https://BANK_WEBSITE.com/net-banking/"+processorRef;

        return new PaymentProcessorResponse.Success(processorRef, redirectRef);
    }
}
