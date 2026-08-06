package dev.pawan.rupixo.vault.service.impl;

import dev.pawan.rupixo.common.entity.Money;
import dev.pawan.rupixo.common.enums.CardBrand;
import dev.pawan.rupixo.common.enums.PaymentMethod;
import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.common.util.RandomizerUtil;
import dev.pawan.rupixo.payment.processor.PaymentProcessorRouter;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorRequest;
import dev.pawan.rupixo.payment.processor.dto.PaymentProcessorResponse;
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

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

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

    @Override
    @Transactional
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CardToken", token));


        VaultCard vaultCard = cardToken.getVaultCard();

        byte[] panBytes = null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest.Card(
                    paymentId,
                    PaymentMethod.CARD,
                    amount,
                    pan,
                    expiry,
                    methodDetails
            );

            PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);

            log.info("Vault charge registered, token={}******", token.substring(0,4));
            return response;
        } catch (Exception e) {
            log.warn("Vault charge failed, token={}******", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if(panBytes != null) Arrays.fill(panBytes, (byte)0);
        }
    }

    private CardBrand detectCardBrand(String pan) {
        if(pan.startsWith("4")) return CardBrand.VISA;
        if(pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if(pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
