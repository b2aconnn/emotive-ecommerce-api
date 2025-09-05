package com.loopers.domain.product.message;

import com.loopers.domain.product.message.model.ProductLikeAddedMessage;
import com.loopers.domain.product.message.model.ProductLikeRemovedMessage;
import com.loopers.domain.product.message.model.ProductViewedMessage;

public interface ProductMessagePublisher {
    void publishLikeAdded(ProductLikeAddedMessage message);
    void publishLikeRemoved(ProductLikeRemovedMessage message);
    void publishViewed(ProductViewedMessage message);
}
