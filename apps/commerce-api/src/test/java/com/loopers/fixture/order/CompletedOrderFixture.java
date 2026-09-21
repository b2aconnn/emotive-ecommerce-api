package com.loopers.fixture.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.dto.OrderCreateInfo;
import com.loopers.domain.product.Product;
import com.loopers.fixture.support.CreatedAtBackdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.loopers.domain.order.OrderStatus.COMPLETED;

/**
 * 집계 대상이 되는 주문(기본 {@link OrderStatus#COMPLETED})을 원하는 생성 시각으로 저장한다.
 * 같은 상품을 여러 라인/여러 주문으로 담는 중복 제거 검증에도 쓴다.
 */
@Component
public class CompletedOrderFixture {

    private static final String ORDERS_TABLE = "orders";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CreatedAtBackdater createdAtBackdater;

    public Order save(Long userId, ZonedDateTime createdAt, Product... products) {
        return save(userId, createdAt, COMPLETED, products);
    }

    public Order save(Long userId, ZonedDateTime createdAt, OrderStatus status, Product... products) {
        Order order = Order.create(new OrderCreateInfo(userId, "주문자", "서울시 강남구", "010-0000-0000", 0L, null));
        applyStatus(order, status);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = Arrays.stream(products)
                .map(product -> OrderItem.create(savedOrder, product, 1L, product.getPrice()))
                .toList();
        orderItemRepository.saveAll(orderItems);

        createdAtBackdater.backdate(ORDERS_TABLE, savedOrder.getId(), createdAt);
        return savedOrder;
    }

    private void applyStatus(Order order, OrderStatus status) {
        switch (status) {
            case CREATED -> { /* Order.create 직후 상태가 CREATED다. */ }
            case PENDING -> order.pending();
            case COMPLETED -> order.complete();
            case CANCELED -> order.cancel();
        }
    }
}
