package com.loopers.domain.order.dto;

public record OrderCreateInfo(
        Long userId,
        String orderer,
        String deliveryAddress,
        String contactNumber
) {}
