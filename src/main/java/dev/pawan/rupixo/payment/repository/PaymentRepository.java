package dev.pawan.rupixo.payment.repository;

import dev.pawan.rupixo.payment.entity.OrderRecord;
import dev.pawan.rupixo.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrder(OrderRecord order);
}
