package com.loopers.infrastructure.payment.jpa;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(Long oderId) {
        return paymentJpaRepository.findByOrderId(oderId);
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return paymentJpaRepository.findByPgOrderId(pgOrderId);
    }
}
