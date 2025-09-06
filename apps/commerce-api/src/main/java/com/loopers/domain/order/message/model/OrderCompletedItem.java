package com.loopers.domain.order.message.model;

public record OrderCompletedItem(
    Long productId,
    Long quantity
) {}
