package com.loopers.domain.order;

import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    public List<OrderItem> saveOrderItems(Order order, List<OrderLineItem> items) {
        List<Long> productIds = items.stream().map(OrderLineItem::productId).toList();
        List<Product> products = productRepository.findByIds(productIds)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (items.size() != products.size()) {
            throw new IllegalArgumentException("상품이 존재하지 않습니다.");
        }

        List<OrderItem> orderItems = items.stream()
                .map(item -> {
                    Product product = products.stream().filter(e -> e.getId().equals(item.productId())).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                    OrderItem orderItem = OrderItem.create(
                            order,
                            product,
                            item.quantity(),
                            calculateTotalPrice(product.getPrice(), item.quantity()));
                    return orderItem;
                })
                .toList();

        return orderItemRepository.saveAll(orderItems);
    }

    private Long calculateTotalPrice(Long productPrice, Long quantity) {
        return productPrice * quantity;
    }
}
