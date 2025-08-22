package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderStatusResult;
import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.PaymentStatus;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.dto.CardType;

import java.util.List;

public class OrderV1Dto {
    public record CreateRequest(Long userId,
                                String orderer,
                                String deliveryAddress,
                                String contactNumber,
                                List<OrderLineItem> items,
                                Long availablePoints,

                                PaymentMethod paymentMethod,
                                CardType cardType,
                                String cardNo) {
        public OrderCreateCommand toCommand() {
            return new OrderCreateCommand(
                userId,
                orderer,
                deliveryAddress,
                contactNumber,
                items,
                availablePoints,

                paymentMethod,
                cardType,
                cardNo
            );
        }
    }

    public record CreateResponse(Long orderId) {
        public static CreateResponse from(Order order) {
            return new CreateResponse(order.getId());
        }
    }

    public record StatusResponse(Long orderId,
                                 PaymentStatus paymentStatus) {
        public static StatusResponse from(OrderStatusResult result) {
            return new StatusResponse(result.orderId(), result.paymentStatus());
        }
    }
}
