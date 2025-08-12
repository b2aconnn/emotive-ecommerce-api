package com.loopers.domain.product.dto.result;

import com.loopers.domain.order.dto.OrderItemCreateCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.dto.command.ProductStocksDeductCommand;

public record ProductQuantityResult(
        Product product,
        Long quantity
) {
    public OrderItemCreateCommand toOrderItemCommands() {
        return new OrderItemCreateCommand(product, quantity);
    }

    public ProductStocksDeductCommand toProductStocksDeductCommands() {
        return new ProductStocksDeductCommand(product, quantity);
    }
}
