package com.loopers.domain.payment.vo;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorTransactionInfoResponse;

public record PgTransactionInfoResult(
        String transactionKey,
        String orderId,
        CardType cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason) {
    public static PgTransactionInfoResult from(PgSimulatorTransactionInfoResponse transactionInfoResponse) {
        return new PgTransactionInfoResult(
                transactionInfoResponse.data().transactionKey(),
                transactionInfoResponse.data().orderId(),
                transactionInfoResponse.data().cardType(),
                transactionInfoResponse.data().cardNo(),
                transactionInfoResponse.data().amount(),
                transactionInfoResponse.data().status(),
                transactionInfoResponse.data().reason());
    }
}
