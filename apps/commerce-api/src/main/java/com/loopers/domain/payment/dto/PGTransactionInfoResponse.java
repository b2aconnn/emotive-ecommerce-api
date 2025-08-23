package com.loopers.domain.payment.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;

public record PGTransactionInfoResponse(
        String transactionKey,
        String orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason
) {}
