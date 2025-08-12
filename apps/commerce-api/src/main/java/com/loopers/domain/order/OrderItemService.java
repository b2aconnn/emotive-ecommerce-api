package com.loopers.domain.order;

import com.loopers.domain.order.dto.OrderItemCreateCommand;
import com.loopers.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderItemService {
    public List<OrderItem> createOrderItems(Order order, List<OrderItemCreateCommand> createCommands) {
        return createCommands.stream()
                .map(command -> OrderItem.create(
                        order,
                        command.product(),
                        command.quantity(),
                        calculateTotalPrice(command.product(), command.quantity())))
                .toList();
    }

    private Long calculateTotalPrice(Product product, Long quantity) {
        return product.getPrice() * quantity;
    }
}
