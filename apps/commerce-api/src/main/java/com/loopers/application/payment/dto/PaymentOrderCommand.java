package com.loopers.application.payment.dto;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.domain.payment.dto.PgRequest;

public record PaymentOrderCommand(Long orderId, PaymentMethod paymentMethod, CardType cardType, String cardNo) {
    public PgRequest toPgRequest(String pgOrderId, Long amount, String callbackUrl) {
        return new PgRequest(pgOrderId, paymentMethod, cardType, cardNo, amount, callbackUrl);
    }
}
