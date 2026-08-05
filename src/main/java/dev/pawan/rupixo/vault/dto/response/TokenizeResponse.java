package dev.pawan.rupixo.vault.dto.response;

import dev.pawan.rupixo.common.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}

