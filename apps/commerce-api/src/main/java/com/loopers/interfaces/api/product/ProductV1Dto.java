package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;
import com.loopers.application.product.ProductsResult;

public class ProductV1Dto {
    public record AllResponse(Long id,
                              String productName,
                              String brandName,
                              Long price,
                              String mainImageUrl) {
        public static AllResponse from(ProductsResult info) {
            if (info == null) {
                return null;
            }
            return new AllResponse(
                    info.id(),
                    info.productName(),
                    info.brandName(),
                    info.price(),
                    info.mainImageUrl()
            );
        }
    }

    public record InfoResponse(Long id,
                               String brandName,
                               String productName,
                               Long price,
                               String mainImageUrl,
                               String description,
                               Integer stockQuantity,
                               Integer likeCount) {
        public static InfoResponse from(ProductResult info) {
            if (info == null) {
                return null;
            }
            return new InfoResponse(
                    info.id(),
                    info.brandName(),
                    info.productName(),
                    info.price(),
                    info.mainImageUrl(),
                    info.description(),
                    info.stockQuantity(),
                    info.likeCount()
            );
        }
    }
}
