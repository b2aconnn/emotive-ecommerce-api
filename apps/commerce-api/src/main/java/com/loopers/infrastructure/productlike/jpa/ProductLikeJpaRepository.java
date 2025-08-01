package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);
}
