package dev.pawan.rupixo.merchant.service;

import dev.pawan.rupixo.merchant.dto.request.CreateApiKeyRequest;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
