package com.loopers.application.order.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.loopers.application.order.OrderService;
import com.loopers.application.order.event.model.OrderCompletedEvent;
import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.application.payment.event.model.PaymentResultEvent;
import com.loopers.domain.order.message.OrderMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderService orderService;

    private final OrderMessagePublisher orderMessagePublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info("Handle payment failure for orderId: {}", event.orderId());

        if (PaymentResultStatus.SUCCESS.equals(event.status())) {
            log.info("Payment successful for orderId: {}, no action needed.", event.orderId());
            orderService.completeOrder(event.orderId());
        } else if (PaymentResultStatus.FAILED.equals(event.status())) {
            log.info("Payment failed for orderId: {}, cancelling order.", event.orderId());
            orderService.cancelOrderWithRestoration(event.orderId());
        } else {
            log.info("Payment status is pending for orderId: {}, no action taken.", event.orderId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompleted(OrderCompletedEvent event) {
        orderMessagePublisher.publishOrderCompleted(event.toCompletedMessage());
    }
}
