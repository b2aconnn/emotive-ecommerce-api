package com.loopers.interfaces.api.order.dto;

import java.time.ZonedDateTime;

import com.loopers.application.order.dto.OrderResult;
import com.loopers.domain.order.OrderStatus;

public record OrdersResponse(Long orderId, OrderStatus status, Long totalAmount, ZonedDateTime orderedAt) {
    public static OrdersResponse from(OrderResult result) {
        return new OrdersResponse(result.id(), result.status(), result.totalAmount(), result.orderedAt());
    }
}
