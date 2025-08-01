package com.loopers.domain.productlike;

import java.util.Optional;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);
    void delete(ProductLike productLike);

    boolean hasUserLikedProduct(Long userId, Long productId);
    Optional<ProductLike> findByUserAndProduct(Long userId, Long productId);
}
