package com.loopers.infrastructure.payment.pgclient.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;

public record PGSimulatorRequestResponse(
    Meta meta,
    Data data
) {
    public record Meta(String result) {}
    public record Data(String transactionKey, PaymentResultStatus status, String reason) {}
}
