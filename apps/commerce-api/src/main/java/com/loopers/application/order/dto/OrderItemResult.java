package com.loopers.application.order.dto;

public record OrderItemResult(
        Long productId,
        String productName,
        Long quantity,
        Long totalPrice
) {}
