package com.loopers.interfaces.api.order.dto;

import com.loopers.domain.order.Order;

public record OrderCreateResponse(Long orderId) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(order.getId());
    }
}
