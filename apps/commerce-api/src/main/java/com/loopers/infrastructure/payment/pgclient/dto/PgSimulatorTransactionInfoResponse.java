package com.loopers.infrastructure.payment.pgclient.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.payment.dto.CardType;

public record PgSimulatorTransactionInfoResponse(Meta meta, Data data) {
    public record Meta(String result, String errorCode, String message) {}

    public record Data(
            String transactionKey,
            String orderId,
            CardType cardType,
            String cardNo,
            Long amount,
            PaymentResultStatus status,
            String reason) {}
}
