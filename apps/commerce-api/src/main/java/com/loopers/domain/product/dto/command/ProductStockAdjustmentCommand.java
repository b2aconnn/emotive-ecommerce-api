package com.loopers.domain.product.dto.command;

import com.loopers.domain.product.Product;

public record ProductStockAdjustmentCommand(
        Product product,
        Long quantity
) {}
