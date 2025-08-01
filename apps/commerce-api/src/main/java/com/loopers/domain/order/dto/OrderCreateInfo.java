package com.loopers.domain.order.dto;

import com.loopers.domain.order.OrderItem;

import java.util.List;

public record OrderCreateInfo(
        Long userId,
        String userName,
        String deliveryAddress,
        String contactNumber,
        List<OrderItem> orderItems
) {}
