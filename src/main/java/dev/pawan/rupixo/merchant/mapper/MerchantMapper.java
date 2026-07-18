package dev.pawan.rupixo.merchant.mapper;

import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;
import dev.pawan.rupixo.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    MerchantResponse toResponse(Merchant merchant);

    Merchant toEntityFromSignUpRequest(MerchantSignupRequest merchantSignupRequest);
}
