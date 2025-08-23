package com.loopers.application.payment.dto;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.domain.payment.dto.PGRequest;

public record PaymentOrderCommand(
    Long orderId,
    PaymentMethod paymentMethod,
    CardType cardType,
    String cardNo
) {
    public PGRequest toPGRequest(String pgOrderId, Long amount, String callbackUrl) {
        return new PGRequest(
            pgOrderId,
            paymentMethod,
            cardType,
            cardNo,
            amount,
            callbackUrl
        );
    }
}
