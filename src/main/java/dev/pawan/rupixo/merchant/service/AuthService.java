package dev.pawan.rupixo.merchant.service;

import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
}
