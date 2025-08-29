package com.loopers.application.coupon.event;

import com.loopers.application.coupon.CouponAppService;
import com.loopers.application.order.event.model.CreateOrderEvent;
import com.loopers.application.order.event.model.OrderCancelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventListener {

    private final CouponAppService couponAppService;

    @TransactionalEventListener
    public void handleUseCoupon(CreateOrderEvent event) {
        log.info("Use Coupon for orderId: {}", event.orderId());

        couponAppService.useCoupon(event.userId(), event.couponId());
    }

    @TransactionalEventListener
    public void handleRestoreCoupon(OrderCancelEvent event) {
        log.info("Restore Coupon for orderId: {}", event.couponId());

        couponAppService.restoreCoupon(event.userId(), event.couponId());
    }
}
