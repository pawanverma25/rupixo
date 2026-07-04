package dev.pawan.rupixo.merchant.dto.request;

import dev.pawan.rupixo.common.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
