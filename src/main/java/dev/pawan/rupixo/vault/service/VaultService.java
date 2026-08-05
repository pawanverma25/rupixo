package dev.pawan.rupixo.vault.service;

import dev.pawan.rupixo.vault.dto.request.TokenizeRequest;
import dev.pawan.rupixo.vault.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(UUID merchantId, @Valid TokenizeRequest tokenizeRequest);
}
