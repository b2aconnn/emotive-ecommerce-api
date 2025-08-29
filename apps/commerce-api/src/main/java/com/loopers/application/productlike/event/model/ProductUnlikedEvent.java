package com.loopers.application.productlike.event.model;

public record ProductUnlikedEvent(
        Long userId,
        Long productId) {}
