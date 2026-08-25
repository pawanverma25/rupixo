package dev.pawan.rupixo.merchant.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
