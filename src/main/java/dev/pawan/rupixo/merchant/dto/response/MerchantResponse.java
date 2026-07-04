package dev.pawan.rupixo.merchant.dto.response;

import dev.pawan.rupixo.common.enums.BusinessType;
import dev.pawan.rupixo.common.enums.MerchantStatus;
import lombok.Data;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
