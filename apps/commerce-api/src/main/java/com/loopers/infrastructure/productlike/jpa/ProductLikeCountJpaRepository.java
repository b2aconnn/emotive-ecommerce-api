package com.loopers.infrastructure.productlike.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.productlike.ProductLikeCount;

public interface ProductLikeCountJpaRepository extends JpaRepository<ProductLikeCount, Long> {
    Optional<ProductLikeCount> findByProductId(Long productId);
}
