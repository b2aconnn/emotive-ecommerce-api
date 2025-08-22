package com.loopers.domain.payment.dto;

public record PaymentResponse(
    String transactionKey,
    String status,
    String reason
) {}
