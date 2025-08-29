package com.loopers.domain.product.vo;


import com.loopers.domain.product.Product;

import java.util.Map;

public record Products(
        Map<Long, Product> products
) {
    public Product get(Long id) {
        return products.get(id);
    }
}
