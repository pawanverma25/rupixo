package dev.pawan.rupixo.payment.dto.request;

import dev.pawan.rupixo.common.entity.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(

        @NotNull
        Money amount,

        @Size(max = 100)
        String receipt,

        Map<String, Object> notes,

        LocalDateTime expiresAt,

        @Valid
        CustomerDetails customerDetails
) {
        public record CustomerDetails(
                @Size(max = 200)
                String name,

                @Email
                @Size(max = 200)
                String email,

                @Size(max = 20)
                String phone
        ) {
        }
}
