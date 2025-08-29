package com.loopers.interfaces.api.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.application.order.dto.OrderStatusResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
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
                                Long couponId,

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
                couponId,

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
                                 OrderStatus status) {
        public static StatusResponse from(OrderStatusResult result) {
            return new StatusResponse(result.orderId(), result.status());
        }
    }
}
