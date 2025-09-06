package com.loopers.application.payment.event;

import com.loopers.application.order.event.model.OrderCreatedEvent;
import com.loopers.application.payment.PaymentAppService;
import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.domain.payment.generator.PgIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventListener {

    private final PaymentAppService paymentAppService;

    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener
    public void handleCreatePayment(OrderCreatedEvent event) {
        log.info("Payment requested for orderId: {}", event.orderId());

        paymentAppService.create(new PaymentCreateCommand(
                event.orderId(),
                PgIdGenerator.generatePgOrderId(),
                event.paymentMethod(),
                event.totalAmount()
        ));
    }
}
