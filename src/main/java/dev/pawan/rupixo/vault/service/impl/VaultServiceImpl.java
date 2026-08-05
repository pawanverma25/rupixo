package dev.pawan.rupixo.vault.service.impl;

import dev.pawan.rupixo.common.enums.CardBrand;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.vault.config.VaultEncryptionConfig;
import dev.pawan.rupixo.vault.dto.request.TokenizeRequest;
import dev.pawan.rupixo.vault.dto.response.TokenizeResponse;
import dev.pawan.rupixo.vault.entity.CardToken;
import dev.pawan.rupixo.vault.entity.VaultCard;
import dev.pawan.rupixo.vault.repository.CardTokenRepository;
import dev.pawan.rupixo.vault.repository.VaultCardRepository;
import dev.pawan.rupixo.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;

    @Override
    @Transactional
    public TokenizeResponse tokenize(UUID merchantId, TokenizeRequest tokenizeRequest) {

        String lastFour = tokenizeRequest.pan().substring(tokenizeRequest.pan().length() - 4);
        String bin = tokenizeRequest.pan().substring(0, 6);
        CardBrand cardBrand = detectCardBrand(tokenizeRequest.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek).encrypt(
                tokenizeRequest.pan().getBytes(StandardCharsets.UTF_8)
        );
        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = vaultCardRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .cardHolderName(tokenizeRequest.cardHolderName())
                .lastFour(lastFour)
                .bin(bin)
                .expiryMonth(tokenizeRequest.expiryMonth().toString())
                .expiryYear(tokenizeRequest.expiryYear().toString())
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .build());

        String token = "tok_" + RandomizerUtil.randomBase64(32);
        CardToken cardToken = CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .customer(null)
                .merchant(null)
                .build();

        cardTokenRepository.save(cardToken);

        return new TokenizeResponse(token, lastFour, cardBrand, tokenizeRequest.expiryMonth(), tokenizeRequest.expiryYear());
    }

    private CardBrand detectCardBrand(String pan) {
        if(pan.startsWith("4")) return CardBrand.VISA;
        if(pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if(pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
