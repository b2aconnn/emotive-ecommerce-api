package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.dto.PaymentResultStatus;

public class PaymentV1Dto {
    public record PgPaymentResultRequest(
            String transactionKey,
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            PaymentResultStatus status,
            String reason
    ) {
        public PaymentResultCommand toCommand() {
            return new PaymentResultCommand(
                    transactionKey,
                    orderId,
                    cardType,
                    cardNo,
                    amount,
                    status,
                    reason
            );
        }
    }
}
