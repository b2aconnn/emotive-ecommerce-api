package com.loopers.domain.payment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.loopers.domain.order.PaymentStatus;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderId(Long oderId);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    Optional<List<Payment>> findByStatusAndCreatedAtBefore(
            PaymentStatus status, ZonedDateTime dateTime, int size, int offset);
}
