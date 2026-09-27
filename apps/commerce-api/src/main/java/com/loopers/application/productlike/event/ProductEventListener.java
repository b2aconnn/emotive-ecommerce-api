package com.loopers.application.productlike.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.loopers.application.product.event.model.ProductViewedEvent;
import com.loopers.application.productlike.ProductLikeService;
import com.loopers.application.productlike.event.model.ProductLikeCountAddedEvent;
import com.loopers.application.productlike.event.model.ProductLikeCountRemovedEvent;
import com.loopers.application.productlike.event.model.ProductLikedEvent;
import com.loopers.domain.product.message.ProductMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductEventListener {

    private final ProductLikeService productLikeService;

    private final ProductMessagePublisher productMessagePublisher;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLike(ProductLikedEvent event) {
        log.info("Product Like. productId: {}", event.productId());
        productLikeService.likeCountUp(event.productId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLikeCountUp(ProductLikeCountAddedEvent event) {
        log.info("Product Like Count Up. productId: {}", event.productId());
        productMessagePublisher.publishLikeAdded(event.toLikeAddedMessage());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnlike(ProductLikedEvent event) {
        log.info("Product Unlike. productId: {}", event.productId());
        productLikeService.unlikeCountDown(event.productId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnlikeCountDown(ProductLikeCountRemovedEvent event) {
        log.info("Product Unlike Count Down. productId: {}", event.productId());
        productMessagePublisher.publishLikeRemoved(event.toLikeRemovedMessage());
    }

    public void handleProductView(ProductViewedEvent event) {
        log.info("Product View. productId: {}", event.productId());
        productMessagePublisher.publishViewed(event.toViewedMessage());
    }
}
