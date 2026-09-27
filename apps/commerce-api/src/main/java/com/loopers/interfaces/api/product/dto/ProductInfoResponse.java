package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.dto.ProductResult;

public record ProductInfoResponse(
        Long id,
        String brandName,
        String productName,
        Long price,
        String mainImageUrl,
        String description,
        Long stockQuantity,
        Integer likeCount) {
    public static ProductInfoResponse from(ProductResult info) {
        return new ProductInfoResponse(
                info.id(),
                info.brandName(),
                info.productName(),
                info.price(),
                info.mainImageUrl(),
                info.description(),
                info.stockQuantity(),
                info.likeCount());
    }
}
