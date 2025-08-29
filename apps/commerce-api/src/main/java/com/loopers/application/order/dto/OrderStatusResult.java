package com.loopers.application.order.dto;

import com.loopers.domain.order.OrderStatus;

public record OrderStatusResult(
        Long orderId,
        OrderStatus status
) {}
