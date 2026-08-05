package dev.pawan.rupixo.vault.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest(
        @NotNull(message = "PAN is required.")
        @LuhnCheck(message = "Invalid card number.")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid PAN length")
        String pan,

        @NotNull(message = "CVV is required.")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV length")
        String cvv,

        @NotNull(message = "Expiry month is required.")
        @Min(value = 1, message = "Invalid expiry month")
        @Max(value = 12, message = "Invalid expiry month")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required.")
        Integer expiryYear,

        @NotNull(message = "Customer Id is required.")
        UUID customerId,

        String cardHolderName
) {
}
