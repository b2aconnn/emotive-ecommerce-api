package com.loopers.domain.productlike;

import java.util.Optional;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);
    void delete(ProductLike productLike);

    boolean existsUserLikedProduct(Long userId, Long productId);
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);
}
