package com.loopers.application.order.dto;

import java.time.ZonedDateTime;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;

public record OrderResult(Long id, OrderStatus status, Long totalAmount, ZonedDateTime orderedAt) {
    public static OrderResult from(Order order) {
        return new OrderResult(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt());
    }
}
