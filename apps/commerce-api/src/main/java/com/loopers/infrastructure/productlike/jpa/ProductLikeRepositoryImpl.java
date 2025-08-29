package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    public ProductLikeRepositoryImpl(ProductLikeJpaRepository productLikeJpaRepository) {
        this.productLikeJpaRepository = productLikeJpaRepository;
    }

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(productLike);
    }

    @Override
    public void delete(ProductLike productLike) {
        productLikeJpaRepository.delete(productLike);
    }

    @Override
    public boolean existsUserLikedProduct(Long userId, Long productId) {
        return productLikeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId) {
        return productLikeJpaRepository.findByUserIdAndProductId(userId, productId);
    }
}
