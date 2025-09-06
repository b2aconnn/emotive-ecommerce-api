package com.loopers.application.order.event.model;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.message.model.OrderCompletedItem;
import com.loopers.domain.order.message.model.OrderCompletedMessage;

import java.util.List;

public record OrderCompletedEvent(
        Long orderId,
        List<OrderItem> orderItems
) {
    public OrderCompletedMessage toCompletedMessage() {
        return new OrderCompletedMessage(
                orderId,
                orderItems.stream()
                    .map(e -> new OrderCompletedItem(
                    e.getProduct().getId(),
                    e.getQuantity()))
                    .toList()
        );
    }
}
