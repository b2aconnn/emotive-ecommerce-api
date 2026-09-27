package com.loopers.application.product.dto;

import java.io.Serializable;

import com.loopers.domain.product.Product;

public record ProductResult(
        Long id,
        String brandName,
        String productName,
        Long price,
        String mainImageUrl,
        String description,
        Long stockQuantity,
        Integer likeCount)
        implements Serializable {
    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getId(),
                product.getBrand().getName(),
                product.getName(),
                product.getPrice(),
                product.getMainImageUrl(),
                product.getDescription(),
                product.getProductStock().getQuantity(),
                product.getProductLikeCount().getLikeCount());
    }
}
