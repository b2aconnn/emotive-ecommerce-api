package com.loopers.application.order.event.model;

public record OrderCanceledEvent(
        Long couponId,
        Long userId) {}
