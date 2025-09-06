package com.loopers.domain.order.message;

import com.loopers.domain.order.message.model.OrderCompletedMessage;

public interface OrderMessagePublisher {
    void publishOrderCompleted(OrderCompletedMessage message);
}
