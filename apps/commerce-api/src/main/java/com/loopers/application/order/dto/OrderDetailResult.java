package com.loopers.application.order.dto;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderDetailResult(
        Long id,
        OrderStatus status,
        String orderer,
        String deliveryAddress,
        String contactNumber,
        Long usedPoints,
        Long totalAmount,
        List<OrderItemResult> items,
        ZonedDateTime orderedAt
) {
    public static OrderDetailResult from(Order order) {
        List<OrderItemResult> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResult(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                ))
                .toList();

        return new OrderDetailResult(
                order.getId(),
                order.getStatus(),
                order.getOrderer(),
                order.getDeliveryAddress(),
                order.getContactNumber(),
                order.getUsedPoints(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt()
        );
    }
}
