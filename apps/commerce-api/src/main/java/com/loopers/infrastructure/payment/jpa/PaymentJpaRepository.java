package com.loopers.infrastructure.payment.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.payment.Payment;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long id);

    Optional<Payment> findByPgOrderId(String pgOrderId);
}
