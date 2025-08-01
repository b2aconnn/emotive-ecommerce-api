package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductLikeCountJpaRepository extends JpaRepository<ProductLikeCount, Long> {
    Optional<ProductLikeCount> findByProductId(Long productId);
}
