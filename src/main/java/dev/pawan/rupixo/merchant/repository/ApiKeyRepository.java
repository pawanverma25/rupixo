package dev.pawan.rupixo.merchant.repository;

import dev.pawan.rupixo.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
}
