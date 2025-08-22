package com.loopers.domain.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCalculator {
    public Long calculateTotalAmount(List<OrderItem> orderItems, Long usePoint) {
        if (orderItems == null || orderItems.isEmpty()) {
            return 0L;
        }

        long oderItemsTotalPrice = orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();

        return oderItemsTotalPrice - usePoint;
    }
}
