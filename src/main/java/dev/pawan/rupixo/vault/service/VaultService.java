package dev.pawan.rupixo.vault.service;

import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
import dev.pawan.rupixo.vault.dto.request.TokenizeRequest;
import dev.pawan.rupixo.vault.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(UUID merchantId, @Valid TokenizeRequest tokenizeRequest);

    PaymentProcessorResponse charge(UUID uuid, String token, Money amount, Map<String, Object> stringObjectMap);
}
