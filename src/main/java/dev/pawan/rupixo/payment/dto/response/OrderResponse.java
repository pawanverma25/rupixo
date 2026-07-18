package dev.pawan.rupixo.payment.dto.response;

import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID merchantId,
        Money amount,
        String receipt,
        OrderStatus status,
        Integer attempts,
        Map<String, Object> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
