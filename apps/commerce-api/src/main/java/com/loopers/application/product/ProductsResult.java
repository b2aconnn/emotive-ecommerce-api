package com.loopers.application.product;

import com.loopers.domain.product.Product;

import java.io.Serializable;

public record ProductsResult(Long id,
                             String productName,
                             String brandName,
                             Long price,
                             String mainImageUrl) implements Serializable {
    public static ProductsResult from(Product product) {
        return new ProductsResult(
            product.getId(),
            product.getName(),
            product.getBrand().getName(),
            product.getPrice(),
            product.getMainImageUrl()
        );
    }
}
