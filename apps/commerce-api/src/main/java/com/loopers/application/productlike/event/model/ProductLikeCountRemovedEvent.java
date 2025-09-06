package com.loopers.application.productlike.event.model;

import com.loopers.domain.product.message.model.ProductLikeRemovedMessage;

public record ProductLikeCountRemovedEvent(
        Long productId,
        Long quantity) {
    public ProductLikeRemovedMessage toLikeRemovedMessage() {
        return new ProductLikeRemovedMessage(
                productId,
                quantity
        );
    }
}
