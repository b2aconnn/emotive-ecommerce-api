package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLikeCount;
import com.loopers.domain.productlike.ProductLikeCountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductLikeCountRepositoryImpl implements ProductLikeCountRepository {

    private final ProductLikeCountJpaRepository productLikeCountJpaRepository;

    public ProductLikeCountRepositoryImpl(ProductLikeCountJpaRepository productLikeCountJpaRepository) {
        this.productLikeCountJpaRepository = productLikeCountJpaRepository;
    }

    @Override
    public ProductLikeCount save(ProductLikeCount productLikeCount) {
        return productLikeCountJpaRepository.save(productLikeCount);
    }

    @Override
    public Optional<ProductLikeCount> findByProductId(Long productId) {
        return productLikeCountJpaRepository.findByProductId(productId);
    }
}
