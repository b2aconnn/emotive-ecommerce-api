package com.loopers.domain.product;

import com.loopers.domain.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductStockService {
    private final ProductStockRepository productStockRepository;

    public Map<Product, ProductStock> reserveProducts(List<OrderItem> orderItems) {
        List<Long> productIds = orderItems.stream().map(e -> e.getProduct().getId()).toList();

        Map<Product, ProductStock> productStocksMap = productStockRepository.findByProductIdsWithLock(productIds)
                .orElseThrow(() -> new IllegalStateException("상품이 존재하지 않거나 재고가 없습니다."))
                .stream()
                .collect(Collectors.toMap(ProductStock::getProduct, Function.identity()));

        for (OrderItem orderItem : orderItems) {
            validateStockEnough(productStocksMap, orderItem);
        }

        return productStocksMap;
    }

    private void validateStockEnough(Map<Product, ProductStock> productStocksMap, OrderItem orderItem) {
        ProductStock productStock = productStocksMap.get(orderItem.getProduct());
        if (productStock == null) {
            throw new IllegalArgumentException("상품 재고가 없습니다.");
        }

        productStock.validateStockEnough(orderItem.getQuantity());
    }

    public void deductStocks(List<OrderItem> orderItems, Map<Product, ProductStock> productStocksMap) {
        for (OrderItem orderItem : orderItems) {
            ProductStock productStock = productStocksMap.get(orderItem.getProduct());
            productStock.deduct(orderItem.getQuantity());
        }
    }
}
