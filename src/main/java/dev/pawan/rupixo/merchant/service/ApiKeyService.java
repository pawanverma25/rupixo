package dev.pawan.rupixo.merchant.service;

import dev.pawan.rupixo.merchant.dto.request.CreateApiKeyRequest;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;

import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);
}
