package dev.pawan.rupixo.payment.repository;

import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.payment.entity.OrderRecord;
import dev.pawan.rupixo.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrder(OrderRecord order);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId AND p.merchantId = :merchantId")
    Optional<Payment> findByIdAndMerchantIdForUpdate(UUID paymentId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId")
    Optional<Payment> findByIdForUpdate(UUID paymentId);
}
