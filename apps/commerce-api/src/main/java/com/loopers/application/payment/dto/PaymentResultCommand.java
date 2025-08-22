package com.loopers.application.payment.dto;

public record PaymentResultCommand(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason
) {}
