package com.loopers.infrastructure.external.dataplatform;

import com.loopers.application.order.event.model.CreateOrderEvent;
import com.loopers.application.payment.event.model.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class DataPlatformEventPublisher {

    private final DataPlatformClient dataPlatformClient;

    @Async
    @TransactionalEventListener
    public void publishOrderData(CreateOrderEvent event) {
        dataPlatformClient.sendOrderData(event);
    }

    @Async
    @TransactionalEventListener
    public void publishPaymentData(PaymentResultEvent payment) {
        dataPlatformClient.sendPaymentData(payment);
    }
}
