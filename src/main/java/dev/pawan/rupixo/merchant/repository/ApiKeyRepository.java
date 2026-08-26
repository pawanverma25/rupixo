package dev.pawan.rupixo.merchant.repository;

import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId);

    Optional<ApiKey> findByMerchant_IdAndId(UUID merchantId, UUID keyId);

    Optional<ApiKey> findByKeyId(String keyId);
}
