package com.loopers.domain.order;

public record Discount(
        Long amount,
        DiscountType type
) {}
