package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> findProducts(List<Long> ids) {
        List<Product> products = productRepository.findByIds(ids)
                .orElse(List.of());

        if (products.size() != ids.size()) {
            throw new IllegalStateException("존재하지 않는 상품이 있습니다.");
        }

        return products;
    }
}
