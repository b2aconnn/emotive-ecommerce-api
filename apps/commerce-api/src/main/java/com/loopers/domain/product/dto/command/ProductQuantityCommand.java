package com.loopers.domain.product.dto.command;

public record ProductQuantityCommand(
        Long productId,
        Long quantity
) {}
