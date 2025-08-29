package com.loopers.application.order.dto;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

import java.util.List;

public record OrderCreateCommand(
    Long userId,
    String orderer,
    String deliveryAddress,
    String contactNumber,
    List<OrderLineItem> items,
    Long usedPoints,

    Long couponId,

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
}
