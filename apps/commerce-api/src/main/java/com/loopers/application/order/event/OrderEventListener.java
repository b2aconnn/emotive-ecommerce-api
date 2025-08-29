package com.loopers.application.order.event;

import com.loopers.application.order.OrderAppService;
import com.loopers.application.payment.event.model.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.loopers.application.payment.dto.PaymentResultStatus.FAILED;
import static com.loopers.application.payment.dto.PaymentResultStatus.SUCCESS;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderAppService orderAppService;

    @TransactionalEventListener
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
}
