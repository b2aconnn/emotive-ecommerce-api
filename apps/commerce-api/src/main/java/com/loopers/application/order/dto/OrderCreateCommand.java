package com.loopers.application.order.dto;

import com.loopers.application.payment.dto.PaymentCreateCommand;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.domain.payment.dto.PGRequest;

import java.util.List;

public record OrderCreateCommand(
    Long userId,
    String orderer,
    String deliveryAddress,
    String contactNumber,
    List<OrderLineItem> items,
    Long usedPoints,

    PaymentMethod paymentMethod,
    CardType cardType,
    String cardNo
) {
    public Order toEntity() {
        return Order.create(new OrderCreateInfo(
            userId,
            orderer,
            deliveryAddress,
            contactNumber,
            usedPoints
        ));
    }

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

    public PaymentCreateCommand toPaymentCreateCommand(
            Long orderId,
            String pgOrderId,
            Long amount) {
        return new PaymentCreateCommand(
            orderId,
            pgOrderId,
            paymentMethod,
            amount
        );
    }
}
