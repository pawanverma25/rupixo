package dev.pawan.rupixo.vault.dto.request;

import jakarta.validation.constraints.*;
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

        UUID customerId,

        @Size(min = 3, message = "Card Holder Name should have at least 3 characters")
        String cardHolderName
) {
}
