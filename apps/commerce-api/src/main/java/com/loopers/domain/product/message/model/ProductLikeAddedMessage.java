package com.loopers.domain.product.message.model;

public record ProductLikeAddedMessage(
    Long productId,
    Long quantity
) {}
