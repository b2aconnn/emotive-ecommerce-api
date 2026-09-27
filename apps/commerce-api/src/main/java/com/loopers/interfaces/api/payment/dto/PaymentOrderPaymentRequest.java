package com.loopers.interfaces.api.payment.dto;

import com.loopers.application.payment.dto.PaymentOrderCommand;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

public record PaymentOrderPaymentRequest(Long orderId, PaymentMethod paymentMethod, CardType cardType, String cardNo) {
    public PaymentOrderCommand toCommand() {
        return new PaymentOrderCommand(orderId, paymentMethod, cardType, cardNo);
    }
}
