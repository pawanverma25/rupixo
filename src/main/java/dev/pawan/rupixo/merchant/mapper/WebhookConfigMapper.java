package dev.pawan.rupixo.merchant.mapper;

import dev.pawan.rupixo.merchant.dto.request.MerchantSignupRequest;
import dev.pawan.rupixo.merchant.dto.response.MerchantResponse;
import dev.pawan.rupixo.merchant.dto.response.WebhookConfigResponse;
import dev.pawan.rupixo.merchant.entity.Merchant;
import dev.pawan.rupixo.merchant.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookConfigMapper {

    @Mapping(source = "rawSecret", target = "webhookSecret")
    WebhookConfigResponse toResponse(MerchantWebhookConfig config, String rawSecret);

}