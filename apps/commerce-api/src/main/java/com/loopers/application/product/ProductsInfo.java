package com.loopers.application.product;

import com.loopers.domain.product.Product;

public record ProductsInfo(Long id,
                           String productName,
                           String brandName,
                           Integer price,
                           String mainImageUrl) {
    public static ProductsInfo from(Product product) {
        return new ProductsInfo(
            product.getId(),
            product.getName(),
            product.getBrand().getName(),
            product.getPrice(),
            product.getMainImageUrl()
        );
    }
}
