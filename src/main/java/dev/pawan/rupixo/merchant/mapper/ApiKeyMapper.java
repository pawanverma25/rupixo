package dev.pawan.rupixo.merchant.mapper;

import dev.pawan.rupixo.merchant.dto.response.ApiKeyCreateResponse;
import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import dev.pawan.rupixo.merchant.repository.ApiKeyRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    ApiKeyResponse toResponse(ApiKey apiKey);

    ApiKeyCreateResponse toCreateResponse(ApiKey apiKey);

    List<ApiKeyResponse> toResponseList(List<ApiKey> apiKeys);
}
