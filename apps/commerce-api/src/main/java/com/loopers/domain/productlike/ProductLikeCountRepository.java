package com.loopers.domain.productlike;

import com.loopers.domain.product.Product;

import java.util.Optional;

public interface ProductLikeCountRepository {
    ProductLikeCount save(ProductLikeCount productLikeCount);

    Optional<ProductLikeCount> findByProductId(Long productId);
}
