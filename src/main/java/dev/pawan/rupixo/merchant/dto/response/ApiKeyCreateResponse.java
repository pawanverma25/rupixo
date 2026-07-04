package dev.pawan.rupixo.merchant.dto.response;

import dev.pawan.rupixo.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment
) {
}
