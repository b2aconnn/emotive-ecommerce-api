package com.loopers.infrastructure.payment.jpa;

import com.loopers.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long id);
    Optional<Payment> findByPgOrderId(String pgOrderId);
}
