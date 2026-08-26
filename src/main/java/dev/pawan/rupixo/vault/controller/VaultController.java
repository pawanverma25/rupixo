package dev.pawan.rupixo.vault.controller;

import dev.pawan.rupixo.merchant.security.MerchantContext;
import dev.pawan.rupixo.vault.dto.request.TokenizeRequest;
import dev.pawan.rupixo.vault.dto.response.TokenizeResponse;
import dev.pawan.rupixo.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    private final MerchantContext merchantContext;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody @Valid TokenizeRequest tokenizeRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenize(merchantContext.getMerchantId(), tokenizeRequest));
    }
}
