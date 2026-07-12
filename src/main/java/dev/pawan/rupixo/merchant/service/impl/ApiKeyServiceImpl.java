package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.merchant.dto.request.CreateApiKeyRequest;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.repository.ApiKeyRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase() + RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(rawSecret) // TODO: encode with BcryptPasswordEncoder
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.environment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchant_Id(merchantId).stream().map(apiKey ->
                new ApiKeyResponse(apiKey.getId(),
                        apiKey.getKeyId(),
                        apiKey.getEnvironment(),
                        apiKey.isEnabled(),
                        apiKey.getLastUsedAt(),
                        null)).
                toList();
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByMerchant_IdAndId(merchantId, keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Api key", keyId));

        apiKey.setEnabled(false);
//        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByMerchant_IdAndId(merchantId, keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Api key", keyId));

        String newRawSecret = RandomizerUtil.randomBase64(40); // TODO: encode with BcryptPasswordEncoder

        apiKey.setPrevoiusKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(newRawSecret);
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), apiKey.getKeyId(), apiKey.getKeySecretHash(), apiKey.getEnvironment());
    }
}



















