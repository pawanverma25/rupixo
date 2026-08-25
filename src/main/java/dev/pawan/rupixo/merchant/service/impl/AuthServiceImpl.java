package dev.pawan.rupixo.merchant.service.impl;

import dev.pawan.rupixo.common.enums.MerchantStatus;
import dev.pawan.rupixo.common.enums.UserRole;
import dev.pawan.rupixo.common.exception.DuplicateResourceException;
import dev.pawan.rupixo.merchant.dto.request.LoginRequest;
import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.LoginResponse;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;
import dev.pawan.rupixo.merchant.entity.AppUser;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.mapper.MerchantMapper;
import dev.pawan.rupixo.merchant.repository.AppUserRepository;
import dev.pawan.rupixo.merchant.repository.MerchantRepository;
import dev.pawan.rupixo.merchant.security.JwtUtil;
import dev.pawan.rupixo.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: " + request.email());
        }


        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AppUser appUser = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.email()));
        String accessToken = jwtUtil.generateAccessToken(appUser.getEmail(), appUser.getMerchant().getId(), appUser.getRole().name());

        return new LoginResponse(accessToken, null);
    }
}















