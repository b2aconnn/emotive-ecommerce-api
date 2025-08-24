package com.loopers.infrastructure.payment.pgclient.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.payment.dto.CardType;

public record PGSimulatorTransactionInfoResponse(
        String transactionKey,
        String orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason
) {}
