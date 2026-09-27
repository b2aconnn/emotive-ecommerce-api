package com.loopers.interfaces.api.order.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.loopers.application.order.dto.OrderDetailResult;
import com.loopers.domain.order.OrderStatus;

public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        String orderer,
        String deliveryAddress,
        String contactNumber,
        Long usedPoints,
        Long totalAmount,
        List<OrderItemResponse> items,
        ZonedDateTime orderedAt) {
    public static OrderDetailResponse from(OrderDetailResult result) {
        List<OrderItemResponse> items =
                result.items().stream().map(OrderItemResponse::from).toList();

        return new OrderDetailResponse(
                result.id(),
                result.status(),
                result.orderer(),
                result.deliveryAddress(),
                result.contactNumber(),
                result.usedPoints(),
                result.totalAmount(),
                items,
                result.orderedAt());
    }
}
