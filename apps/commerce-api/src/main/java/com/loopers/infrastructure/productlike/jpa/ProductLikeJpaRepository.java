package com.loopers.infrastructure.productlike.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.productlike.ProductLike;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);
}
