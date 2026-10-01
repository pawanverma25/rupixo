package dev.pawan.rupixo.merchant.api;

import dev.pawan.rupixo.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantWebhookApi {
    List<WebhookTarget> getActiveConfigByEventType(UUID merchantId, String eventType);
}
