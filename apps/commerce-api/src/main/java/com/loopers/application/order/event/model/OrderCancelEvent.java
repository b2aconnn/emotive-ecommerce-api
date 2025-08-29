package com.loopers.application.order.event.model;

public record OrderCancelEvent(
        Long couponId,
        Long userId) {}
