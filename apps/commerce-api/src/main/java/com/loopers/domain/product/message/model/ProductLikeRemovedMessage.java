package com.loopers.domain.product.message.model;

public record ProductLikeRemovedMessage(
    Long productId,
    Long quantity
) {}
