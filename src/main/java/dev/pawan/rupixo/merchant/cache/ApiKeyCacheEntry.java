package dev.pawan.rupixo.merchant.cache;

import dev.pawan.rupixo.common.enums.Environment;
import dev.pawan.rupixo.merchant.entity.Merchant;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(
        UUID id,
        UUID merchantId,
        String keyId,
        String keySecretHash,
        String previousKeySecretHash,
        Environment environment,
        boolean enabled,
        LocalDateTime gracePeriodExpiresAt
) implements Serializable {
    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt != null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }
}
