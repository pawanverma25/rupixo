package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.merchant.cache.ApiKeyCache;
import dev.pawan.rupixo.merchant.dto.request.CreateApiKeyRequest;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.mapper.ApiKeyMapper;
import dev.pawan.rupixo.merchant.repository.ApiKeyRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final ApiKeyMapper apiKeyMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_"+request.environment().name().toLowerCase() + "_" + RandomizerUtil.randomBase64(32);
        String rawSecret = RandomizerUtil.randomBase64(40);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(passwordEncoder.encode(rawSecret))
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

            return new ApiKeyCreateResponse(
                apiKey.getId(),
                apiKey.getKeyId(),
                rawSecret,
                apiKey.getEnvironment()
        );
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        List<ApiKey> apiKeyList =  apiKeyRepository.findByMerchant_Id(merchantId);
        return apiKeyMapper.toResponseList(apiKeyList);
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByMerchant_IdAndId(merchantId, keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Api key", keyId));

        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByMerchant_IdAndId(merchantId, keyId)
                .orElseThrow(() -> new ResourceNotFoundException("Api key", keyId));

        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot rotate a disabled API key.");

        String newRawSecret = RandomizerUtil.randomBase64(40);

        apiKey.setPrevoiusKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(passwordEncoder.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());

        return apiKeyMapper.toCreateResponse(apiKey);
    }
}



















