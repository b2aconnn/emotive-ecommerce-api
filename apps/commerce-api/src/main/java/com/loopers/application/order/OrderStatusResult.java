package com.loopers.application.order;

import com.loopers.domain.order.PaymentStatus;

public record OrderStatusResult(
        Long orderId,
        PaymentStatus paymentStatus
) {}
