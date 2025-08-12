package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderAppService {

    private final OrderService orderService;

    private final UserService userService;

    @Transactional
    public Order order(OrderCreateCommand createCommand) {
        userService.existsById(createCommand.userId());

        Order order = orderService.createOrder(createCommand);

        return order;
    }
}
