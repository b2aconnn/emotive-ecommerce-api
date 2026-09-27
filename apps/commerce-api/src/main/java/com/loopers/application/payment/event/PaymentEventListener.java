package com.loopers.application.payment.event;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.loopers.application.order.event.model.OrderCreatedEvent;
import com.loopers.application.payment.PaymentService;
import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.domain.payment.generator.PgIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventListener {

    private final PaymentService paymentService;

    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener
    public void handleCreatePayment(OrderCreatedEvent event) {
        log.info("Payment requested for orderId: {}", event.orderId());

        paymentService.create(new PaymentCreateCommand(
                event.orderId(), PgIdGenerator.generatePgOrderId(), event.paymentMethod(), event.totalAmount()));
    }
}
