package com.loopers.application.order.dto;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;
import com.loopers.domain.payment.dto.PaymentRequest;
import com.loopers.domain.payment.generator.PgIdGenerator;

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

    public PaymentRequest toPaymentRequest(Long amount, String callbackUrl) {
        return new PaymentRequest(
            PgIdGenerator.generatePgOrderId(),
            paymentMethod,
            cardType,
            cardNo,
            amount,
            callbackUrl
        );
    }
}
