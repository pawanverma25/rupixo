package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.merchant.dto.request.CreateApiKeyRequest;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.repository.ApiKeyRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_"+request.environment().name().toUpperCase()+"big_random_string";
        String rawSecret = "big_random_secret"; // TODO: replace with cryptographic random hex
//        a-z,A-Z,0-9,-,_
//        a-z,0-9

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(rawSecret) // TODO: encode with BcryptPasswordEncoder
                .environment(request.environment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.environment());
    }
}



















