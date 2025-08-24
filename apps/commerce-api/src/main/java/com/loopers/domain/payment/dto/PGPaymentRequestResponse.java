package com.loopers.domain.payment.dto;

import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequestResponse;

public record PGPaymentRequestResponse(
        String transactionKey,
        PaymentResultStatus status,
        String reason
) {
    public static PGPaymentRequestResponse from(PGSimulatorRequestResponse pgSimulatorRequestResponse) {
        return new PGPaymentRequestResponse(
                pgSimulatorRequestResponse.data().transactionKey(),
                pgSimulatorRequestResponse.data().status(),
                pgSimulatorRequestResponse.data().reason()
        );
    }
}
