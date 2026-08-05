package dev.pawan.rupixo.vault.repository;

import dev.pawan.rupixo.vault.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardTokenRepository extends JpaRepository<CardToken, UUID> {
}
