package dev.pawan.rupixo.operations.repository;

import dev.pawan.rupixo.operations.entity.DlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DlqEventRepository extends JpaRepository<DlqEvent, UUID> {
}
