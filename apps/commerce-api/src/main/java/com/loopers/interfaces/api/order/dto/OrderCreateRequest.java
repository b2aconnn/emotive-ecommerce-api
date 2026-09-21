package com.loopers.interfaces.api.order.dto;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

import java.util.List;

public record OrderCreateRequest(String orderer,
                            String deliveryAddress,
                            String contactNumber,
                            List<OrderLineItem> items,
                            Long availablePoints,
                            Long couponId,

                            PaymentMethod paymentMethod,
                            CardType cardType,
                            String cardNo) {
    public OrderCreateCommand toCommand() {
        return new OrderCreateCommand(
            orderer,
            deliveryAddress,
            contactNumber,
            items,
            availablePoints,
            couponId,

            paymentMethod,
            cardType,
            cardNo
        );
    }
}
