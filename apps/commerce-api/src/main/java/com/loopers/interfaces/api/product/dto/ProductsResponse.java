package com.loopers.interfaces.api.product.dto;

import com.loopers.application.product.dto.ProductsResult;

public record ProductsResponse(Long id,
                              String productName,
                              String brandName,
                              Long price,
                              String mainImageUrl) {
    public static ProductsResponse from(ProductsResult info) {
        if (info == null) {
            return null;
        }
        return new ProductsResponse(
                info.id(),
                info.productName(),
                info.brandName(),
                info.price(),
                info.mainImageUrl()
        );
    }
}
