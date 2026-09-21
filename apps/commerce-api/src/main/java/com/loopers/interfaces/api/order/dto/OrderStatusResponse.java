package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.domain.order.OrderStatus;

public record OrderStatusResponse(Long orderId,
                             OrderStatus status) {
    public static OrderStatusResponse from(OrderStatusResult result) {
        return new OrderStatusResponse(result.orderId(), result.status());
    }
}
