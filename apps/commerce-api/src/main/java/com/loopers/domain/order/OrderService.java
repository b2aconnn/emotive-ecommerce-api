package com.loopers.domain.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderItemService orderItemService;

    private final PointRepository pointRepository;

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(OrderCreateCommand createCommand) {
        Order saveOrder = orderRepository.save(createCommand.toEntity());
        List<OrderItem> orderItems = orderItemService.saveOrderItems(saveOrder, createCommand.items());
        saveOrder.updateOrderItems(orderItems);

        return saveOrder;
    }

    @Transactional
    public void cancelOrderWithRestoration(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        orderItemService.restoreProductStocks(order.getOrderItems());

        Point point = pointRepository.findByUserId(order.getUserId())
                .orElseThrow(() -> new IllegalStateException("포인트가 존재하지 않습니다."));
        point.restorePoint(order.getUsedPoints());

        // 쿠폰 복원

        order.cancel();
    }
}
