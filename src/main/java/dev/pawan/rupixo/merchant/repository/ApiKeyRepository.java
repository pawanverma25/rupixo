package dev.pawan.rupixo.merchant.repository;

import dev.pawan.rupixo.merchant.dto.response.ApiKeyResponse;
import dev.pawan.rupixo.merchant.entity.ApiKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId);

    Optional<ApiKey> findByMerchant_IdAndId(UUID merchantId, UUID keyId);

    Optional<ApiKey> findByKeyId(String keyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApiKey a WHERE a.merchant.id = :merchantId AND a.id = :keyId")
    Optional<ApiKey> findByMerchant_IdAndIdForUpdate(UUID merchantId, UUID keyId);
}
