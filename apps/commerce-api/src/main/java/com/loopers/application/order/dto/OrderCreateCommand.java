package com.loopers.application.order.dto;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.domain.product.dto.command.ProductQuantityCommand;

import java.util.List;

public record OrderCreateCommand(
    Long userId,
    String orderer,
    String deliveryAddress,
    String contactNumber,
    List<OrderLineItem> items,
    Long availablePoints
) {
    public Order toEntity() {
        return Order.create(new OrderCreateInfo(
            userId,
            orderer,
            deliveryAddress,
            contactNumber
        ));
    }

    public List<ProductQuantityCommand> toProductQuantityCommands() {
        return items.stream()
                .map(item -> new ProductQuantityCommand(item.productId(), item.quantity())).toList();
    }
}
