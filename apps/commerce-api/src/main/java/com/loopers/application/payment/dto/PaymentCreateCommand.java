package com.loopers.application.payment.dto;

import com.loopers.domain.payment.PaymentMethod;

public record PaymentCreateCommand(
    Long orderId,
    String pgOrderId,
    PaymentMethod paymentMethod,
    Long amount
) {
}
