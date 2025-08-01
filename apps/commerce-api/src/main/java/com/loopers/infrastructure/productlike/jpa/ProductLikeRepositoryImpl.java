package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.product.Product;
import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeCount;
import com.loopers.domain.productlike.ProductLikeCountRepository;
import com.loopers.domain.productlike.ProductLikeRepository;
import com.loopers.domain.user.User;
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
    public boolean hasUserLikedProduct(Long userId, Long productId) {
        return productLikeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<ProductLike> findByUserAndProduct(Long userId, Long productId) {
        return productLikeJpaRepository.findByUserIdAndProductId(userId, productId);
    }
}
