package com.loopers.domain.payment.dto;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.pgclient.dto.PGSimulatorRequest;

public record PGRequest(
    String orderId,
    PaymentMethod paymentMethod,
    CardType cardType,
    String cardNo,
    Long amount,
    String callbackUrl
) {
    public PGSimulatorRequest toSimulatorRequest() {
        return new PGSimulatorRequest(
            this.orderId,
            this.paymentMethod,
            this.cardType,
            this.cardNo,
            this.amount,
            this.callbackUrl
        );
    }
}
