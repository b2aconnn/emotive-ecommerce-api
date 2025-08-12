package com.loopers.application.order.dto;

public record OrderLineItem(
    Long productId,
    Long quantity
) {}
