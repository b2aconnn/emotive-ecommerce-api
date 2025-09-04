package com.loopers.domain.product;

import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.product.vo.Products;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductStockRepository productStockRepository;

    public Products reserveProducts(List<Long> productIds) {
        productStockRepository.findByProductIdsWithStockLock(productIds);

        Map<Long, Product> productsMap = productRepository.findByIdInWithStock(productIds)
                .orElseThrow(() -> new IllegalStateException("상품이 존재하지 않거나 재고가 없습니다."))
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return new Products(productsMap);
    }

    public void deductStocks(Products products, List<OrderLineItem> orderLineItems) {
        for (OrderLineItem orderLineItem : orderLineItems) {
            Product product = products.get(orderLineItem.productId());
            product.getProductStock().deduct(orderLineItem.quantity());
        }
    }
}
