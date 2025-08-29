package com.loopers.domain.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.vo.Products;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductService productService;

    @Transactional
    public Order saveOrder(OrderCreateCommand createCommand, Products products) {
        Order saveOrder = orderRepository.save(createCommand.toEntity());
        List<OrderItem> orderItems = saveOrderItems(products, saveOrder, createCommand.items());
        saveOrder.updateOrderItems(orderItems);

        saveOrder.calculateTotalAmount(createCommand.usedPoints());

        return saveOrder;
    }

    private List<OrderItem> saveOrderItems(Products products, Order order, List<OrderLineItem> items) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderLineItem item : items) {
            Product product = products.get(item.productId());
            if (product == null) {
                throw new IllegalArgumentException("상품이 존재하지 않습니다.");
            }

            if (!product.isStockEnough(item.quantity())) {
                throw new IllegalArgumentException("상품 재고가 부족합니다. 상품ID: " + product.getId());
            }

            Long totalPrice = calculateTotalPrice(product.getPrice(), item.quantity());
            OrderItem orderItem = OrderItem.create(
                    order,
                    product,
                    item.quantity(),
                    totalPrice
            );
            orderItems.add(orderItem);
        }

        return orderItemRepository.saveAll(orderItems);
    }

    private Long calculateTotalPrice(Long productPrice, Long quantity) {
        return productPrice * quantity;
    }

    public void restoreProductStocks(List<OrderItem> orderItems) {
        List<Long> productIds = orderItems.stream().map(e -> e.getProduct().getId()).toList();
        Products products = productService.reserveProducts(productIds);
        for (OrderItem orderItem : orderItems) {
            Product product = products.get(orderItem.getProduct().getId());
            if (product == null) {
                throw new IllegalArgumentException("상품 재고가 존재하지 않습니다: " + orderItem.getProduct());
            }

            product.getProductStock().restoreStock(orderItem.getQuantity());
        }
    }
}
