package com.loopers.domain.payment.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorTransactionInfoResponse;

public record PGTransactionInfoResponse(
        String transactionKey,
        String orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason
) {
    public static PGTransactionInfoResponse from(PGSimulatorTransactionInfoResponse transactionInfoResponse) {
        return new PGTransactionInfoResponse(
                transactionInfoResponse.transactionKey(),
                transactionInfoResponse.orderId(),
                transactionInfoResponse.cardType(),
                transactionInfoResponse.cardNo(),
                transactionInfoResponse.amount(),
                transactionInfoResponse.status(),
                transactionInfoResponse.reason()
        );
    }
}
