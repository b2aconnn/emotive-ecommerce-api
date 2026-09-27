package com.loopers.domain.product.vo;

import java.util.Map;

import com.loopers.domain.product.Product;

public record Products(Map<Long, Product> products) {
    public Product get(Long id) {
        return products.get(id);
    }
}
