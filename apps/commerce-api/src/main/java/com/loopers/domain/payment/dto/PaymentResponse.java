package com.loopers.domain.payment.dto;

public record PaymentResponse(
    Long transactionKey,
    TransactionStatus status,
    String reason
) {}
