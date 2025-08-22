package com.loopers.application.payment.dto;

import com.loopers.domain.order.PaymentStatus;

public record PaymentStatusResult(
    Long orderId,
    PaymentStatus paymentStatus
) {}
