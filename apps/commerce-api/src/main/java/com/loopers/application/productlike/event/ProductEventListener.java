package com.loopers.application.productlike.event;

import com.loopers.application.productlike.ProductLikeAppService;
import com.loopers.application.productlike.event.model.ProductLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductEventListener {

    private final ProductLikeAppService productLikeAppService;

    @Async
    @TransactionalEventListener
    public void handleProductLikeCountUp(ProductLikedEvent event) {
        log.info("Product Like Count Up. productId: {}", event.productId());
        productLikeAppService.likeCountUp(event.productId());
    }

    @Async
    @TransactionalEventListener
    public void handleProductUnlikeCountDown(ProductLikedEvent event) {
        log.info("Product Unlike Count Down. productId: {}", event.productId());
        productLikeAppService.unlikeCountDown(event.productId());
    }
}
