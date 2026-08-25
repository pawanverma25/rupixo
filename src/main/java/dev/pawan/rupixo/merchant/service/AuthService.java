package dev.pawan.rupixo.merchant.service;

import dev.pawan.rupixo.merchant.dto.request.LoginRequest;
import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.LoginResponse;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;
import jakarta.validation.Valid;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
    LoginResponse login(LoginRequest request);
}
