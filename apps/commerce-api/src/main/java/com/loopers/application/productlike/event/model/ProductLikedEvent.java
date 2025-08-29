package com.loopers.application.productlike.event.model;

public record ProductLikedEvent(
        Long userId,
        Long productId) {}
