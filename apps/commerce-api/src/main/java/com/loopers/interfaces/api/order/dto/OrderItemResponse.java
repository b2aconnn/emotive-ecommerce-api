package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.dto.OrderItemResult;

public record OrderItemResponse(Long productId,
                                 String productName,
                                 Long quantity,
                                 Long totalPrice) {
    public static OrderItemResponse from(OrderItemResult result) {
        return new OrderItemResponse(
                result.productId(),
                result.productName(),
                result.quantity(),
                result.totalPrice()
        );
    }
}
