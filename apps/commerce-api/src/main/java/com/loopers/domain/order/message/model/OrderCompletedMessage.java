package com.loopers.domain.order.message.model;

import java.util.List;

public record OrderCompletedMessage(
    Long orderId,
    List<OrderCompletedItem> orderItems
) {}
