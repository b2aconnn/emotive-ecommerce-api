package com.loopers.application.payment.dto;

public record PaymentHandleResultCommand(
    String pgOrderId,
    String transactionKey,
    PaymentResultStatus status,
    String reason
) {}
