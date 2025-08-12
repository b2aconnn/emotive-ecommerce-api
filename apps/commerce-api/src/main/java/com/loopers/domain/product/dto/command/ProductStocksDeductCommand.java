package com.loopers.domain.product.dto.command;

import com.loopers.domain.product.Product;

public record ProductStocksDeductCommand(
        Product product,
        Long quantity
) {}
