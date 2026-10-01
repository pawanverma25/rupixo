package dev.pawan.rupixo.merchant.api.impl;

import dev.pawan.rupixo.merchant.api.MerchantWebhookApi;
import dev.pawan.rupixo.common.dto.WebhookTarget;
import dev.pawan.rupixo.merchant.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantWebhookApiImpl implements MerchantWebhookApi {

    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor masterKeyEncryptor;

    @Override
    public List<WebhookTarget> getActiveConfigByEventType(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(config -> config.isSubscribedToEventType(eventType))
                .map(config -> {
                    String encryptedSecret = config.getWebhookSecret();
                    byte[] decryptedSecretBytes = masterKeyEncryptor.decrypt(encryptedSecret.getBytes());
                    return new WebhookTarget(config.getId(), config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                })
                .collect(Collectors.toList());
    }
}
