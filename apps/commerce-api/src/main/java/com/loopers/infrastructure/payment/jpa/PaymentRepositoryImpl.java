package com.loopers.infrastructure.payment.jpa;

import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.loopers.domain.payment.QPayment.payment;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    private JPAQueryFactory queryFactory;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository,
                                      EntityManager em) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

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

    @Override
    public Optional<List<Payment>> findByStatusAndCreatedAtBefore(
            PaymentStatus status,
            ZonedDateTime dateTime,
            int size,
            int offset) {
        return Optional.ofNullable(queryFactory.select(payment)
                .from(payment)
                .where(payment.status.eq(status)
                        .and(payment.createdAt.before(dateTime)))
                .orderBy(payment.createdAt.asc())
                .offset(offset)
                .limit(size)
                .fetch());
    }
}
