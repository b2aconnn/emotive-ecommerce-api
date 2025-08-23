package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.dto.PaymentOrderCommand;
import com.loopers.application.payment.dto.PaymentResultCommand;
import com.loopers.application.payment.dto.PaymentResultStatus;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

public class PaymentV1Dto {
    public record PgResultRequest(
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

    public record OrderPaymentRequest(
            Long orderId,
            PaymentMethod paymentMethod,
            CardType cardType,
            String cardNo
    ) {
        public PaymentOrderCommand toCommand() {
            return new PaymentOrderCommand(
                    orderId,
                    paymentMethod,
                    cardType,
                    cardNo
            );
        }
    }
}
