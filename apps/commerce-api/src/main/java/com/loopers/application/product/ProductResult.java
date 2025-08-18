package com.loopers.application.product;

import com.loopers.domain.product.Product;

import java.io.Serializable;

public record ProductResult(Long id,
                            String brandName,
                            String productName,
                            Long price,
                            String mainImageUrl,
                            String description,
                            Integer stockQuantity,
                            Integer likeCount) implements Serializable {
    public static ProductResult from(Product product) {
        return new ProductResult(
            product.getId(),
            product.getBrand().getName(),
            product.getName(),
            product.getPrice(),
            product.getMainImageUrl(),
            product.getDescription(),
            product.getProductStock().getQuantity(),
            product.getProductLikeCount().getLikeCount()
        );
    }
}
