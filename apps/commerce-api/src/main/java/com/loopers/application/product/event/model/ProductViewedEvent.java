package com.loopers.application.product.event.model;

import com.loopers.domain.product.message.model.ProductViewedMessage;

public record ProductViewedEvent(
    Long productId
) {
    public ProductViewedMessage toViewedMessage() {
        return new ProductViewedMessage(productId);
    }
}
