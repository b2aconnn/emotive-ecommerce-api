package com.loopers.application.order.event;

import com.loopers.application.order.OrderAppService;
import com.loopers.application.order.event.model.OrderCompletedEvent;
import com.loopers.application.payment.event.model.PaymentResultEvent;
import com.loopers.domain.order.message.OrderMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.loopers.application.payment.dto.PaymentResultStatus.FAILED;
import static com.loopers.application.payment.dto.PaymentResultStatus.SUCCESS;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderAppService orderAppService;

    private final OrderMessagePublisher orderMessagePublisher;

    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handlePaymentResult(PaymentResultEvent event) {
        log.info("Handle payment failure for orderId: {}", event.orderId());

        if (SUCCESS.equals(event.status())) {
            log.info("Payment successful for orderId: {}, no action needed.", event.orderId());
            orderAppService.completeOrder(event.orderId());
        } else if (FAILED.equals(event.status())) {
            log.info("Payment failed for orderId: {}, cancelling order.", event.orderId());
            orderAppService.cancelOrderWithRestoration(event.orderId());
        } else {
            log.info("Payment status is pending for orderId: {}, no action taken.", event.orderId());
        }
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleCompleted(OrderCompletedEvent event) {
        orderMessagePublisher.publishOrderCompleted(event.toCompletedMessage());
    }
}
