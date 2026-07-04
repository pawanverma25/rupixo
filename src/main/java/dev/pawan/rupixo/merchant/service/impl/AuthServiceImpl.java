package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.enums.MerchantStatus;
import dev.pawan.rupixo.common.enums.UserRole;
import dev.pawan.rupixo.common.exception.DuplicateResourceException;
import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;
import dev.pawan.rupixo.merchant.entity.AppUser;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.repository.AppUserRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: " + request.email());
        }

        Merchant merchant = Merchant.builder()
                .businessName(request.businessName())
                .businessType(request.businessType())
                .name(request.name())
                .email(request.email())
                .status(MerchantStatus.PENDING_KYC)
                .build();
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(request.password()) // TODO: encrypt using Bcrypt
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return new MerchantResponse(merchant.getId(), merchant.getName(),
                merchant.getEmail(), merchant.getBusinessName(),
                merchant.getBusinessType(), merchant.getStatus());
    }
}















