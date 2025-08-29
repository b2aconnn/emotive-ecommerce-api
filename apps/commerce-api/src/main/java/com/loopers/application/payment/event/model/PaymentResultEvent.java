package com.loopers.application.payment.event.model;

import com.loopers.application.payment.dto.PaymentResultStatus;

public record PaymentResultEvent(
        Long orderId,
        PaymentResultStatus status) {}
