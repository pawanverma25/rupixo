package dev.pawan.rupixo.payment.config;

import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.payment.gateway.PaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class PaymentAdapterConfig {

    // gets all the implementations of the PaymentAdapter and adds them to this map
    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(List<PaymentAdapter> paymentAdapterList){
        return paymentAdapterList.stream().collect(Collectors.toMap(
                PaymentAdapter::getPaymentMethod,
                Function.identity()
        ));
    }
}
