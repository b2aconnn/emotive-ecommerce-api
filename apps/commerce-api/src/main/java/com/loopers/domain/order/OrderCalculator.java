package com.loopers.domain.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCalculator {
    public Long calculateTotalAmount(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0L;
        }

        return orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();
    }
}
