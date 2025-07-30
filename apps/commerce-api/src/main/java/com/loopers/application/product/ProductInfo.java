package com.loopers.application.product;

import com.loopers.domain.product.Product;

public record ProductInfo(Long id,
                          String brandName,
                          String productName,
                          Integer price,
                          String mainImageUrl,
                          String description,
                          Integer stockQuantity,
                          Integer likeCount) {
    public static ProductInfo from(Product product) {
        return new ProductInfo(
            product.getId(),
            product.getBrand().getName(),
            product.getName(),
            product.getPrice(),
            product.getMainImageUrl(),
            product.getDescription(),
            product.getStockQuantity(),
            product.getLikeCount()
        );
    }
}
