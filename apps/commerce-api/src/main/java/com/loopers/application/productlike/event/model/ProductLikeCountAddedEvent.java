package com.loopers.application.productlike.event.model;

import com.loopers.domain.order.message.model.OrderCompletedItem;
import com.loopers.domain.order.message.model.OrderCompletedMessage;
import com.loopers.domain.product.message.model.ProductLikeAddedMessage;

public record ProductLikeCountAddedEvent(
        Long productId,
        Long quantity) {
    public ProductLikeAddedMessage toLikeAddedMessage() {
        return new ProductLikeAddedMessage(
                productId,
                quantity
        );
    }
}
