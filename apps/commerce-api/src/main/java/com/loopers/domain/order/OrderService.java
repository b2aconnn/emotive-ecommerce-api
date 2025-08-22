package com.loopers.domain.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderItemService orderItemService;

    private final OrderRepository orderRepository;

    public Order createOrder(OrderCreateCommand createCommand) {
        Order saveOrder = orderRepository.save(createCommand.toEntity());
        List<OrderItem> orderItems = orderItemService.saveOrderItems(saveOrder, createCommand.items());
        saveOrder.updateOrderItems(orderItems);

        return saveOrder;
    }
}
