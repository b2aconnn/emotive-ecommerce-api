package com.loopers.interfaces.api.payment.dto;

import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.dto.PaymentResultStatus;

public record PaymentPgResultRequest(
        String transactionKey,
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        PaymentResultStatus status,
        String reason) {
    public PaymentResultCommand toCommand() {
        return new PaymentResultCommand(transactionKey, orderId, cardType, cardNo, amount, status, reason);
    }
}
