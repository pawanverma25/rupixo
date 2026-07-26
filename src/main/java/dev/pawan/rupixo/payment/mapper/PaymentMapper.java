package dev.pawan.rupixo.payment.mapper;

import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> paymentList);

    @Mapping(source = "order.id", target = "orderId")
    PaymentRequest toPaymentRequest(Payment payment);

}
