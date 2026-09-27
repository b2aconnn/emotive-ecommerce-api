package com.loopers.domain.payment.dto;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.pgclient.dto.PgSimulatorRequest;

public record PgRequest(
        String orderId,
        PaymentMethod paymentMethod,
        CardType cardType,
        String cardNo,
        Long amount,
        String callbackUrl) {
    public PgSimulatorRequest toSimulatorRequest() {
        return new PgSimulatorRequest(
                this.orderId, this.paymentMethod, this.cardType, this.cardNo, this.amount, this.callbackUrl);
    }
}
