package com.loopers.domain.order.dto;

import com.loopers.domain.product.Product;

public record OrderItemCreateCommand(
        Product product,
        Long quantity
) {}
