package com.loopers.application.coupon.event;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.order.event.model.OrderCreatedEvent;
import com.loopers.application.order.event.model.OrderCanceledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventListener {

    private final CouponService couponService;

    @TransactionalEventListener
    public void handleUseCoupon(OrderCreatedEvent event) {
        log.info("Use Coupon for orderId: {}", event.orderId());

        couponService.useCoupon(event.userId(), event.couponId());
    }

    @TransactionalEventListener
    public void handleRestoreCoupon(OrderCanceledEvent event) {
        log.info("Restore Coupon for orderId: {}", event.couponId());

        couponService.restoreCoupon(event.userId(), event.couponId());
    }
}
