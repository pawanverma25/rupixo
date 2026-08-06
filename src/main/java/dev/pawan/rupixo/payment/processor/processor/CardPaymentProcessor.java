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
public class CardPaymentProcessor implements PaymentProcessor {

    private static final String PAN_CARD_DECLINED = "4000000000003";
    private static final String PAN_CARD_EXPIRED = "4000000000002";

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        if(request instanceof PaymentProcessorRequest.Card cardRequest){
            String pan = cardRequest.pan();
            if(PAN_CARD_DECLINED.equals(pan)){
                log.warn("Card declined");
                return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined by bank.");
            } else if(PAN_CARD_EXPIRED.equals(pan)){
                log.warn("Card expired");
                return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired.");
            }
        } else {
            throw new IllegalArgumentException("Noncard request sent to CardPaymentProcessor");
        }
        String processorRef = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
