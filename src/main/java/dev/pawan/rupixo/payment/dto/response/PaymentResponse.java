package dev.pawan.rupixo.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.common.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
    UUID id,
    UUID orderId,
    UUID merchantId,
    Money amount,
    PaymentStatus status,
    PaymentMethod method,
    Map<String, Object> methodDetails,
    String errorCode,
    String errorDescription,
    LocalDateTime capturedAt,
    LocalDateTime createdAt
) {
}
