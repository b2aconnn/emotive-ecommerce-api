package com.loopers.application.payment.event;

import com.loopers.application.order.event.model.CreateOrderEvent;
import com.loopers.application.payment.PaymentAppService;
import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.domain.payment.generator.PgIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventListener {

    private final PaymentAppService paymentAppService;

    @TransactionalEventListener
    public void handleCreatePayment(CreateOrderEvent event) {
        log.info("Payment requested for orderId: {}", event.orderId());

        paymentAppService.create(new PaymentCreateCommand(
                event.orderId(),
                PgIdGenerator.generatePgOrderId(),
                event.paymentMethod(),
                event.totalAmount()
        ));
    }
}
