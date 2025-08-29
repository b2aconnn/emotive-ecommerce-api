package com.loopers.application.productlike;

import com.loopers.application.productlike.event.model.ProductLikedEvent;
import com.loopers.application.productlike.event.model.ProductUnlikedEvent;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductLikeAppService {

    private final ProductLikeRepository productLikeRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void likeProduct(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "유저가 존재하지 않습니다. userId: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "상품이 존재하지 않습니다. productId: " + productId));

        Optional<ProductLike> productLikeOptional =
                productLikeRepository.findByUserIdAndProductId(user.getId(), product.getId());
        if (productLikeOptional.isPresent()) {
            return;
        }

        productLikeRepository.save(ProductLike.create(user, product));

        applicationEventPublisher.publishEvent(new ProductLikedEvent(userId, productId));
    }

    @Transactional
    public void likeCountUp(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "상품이 존재하지 않습니다. productId: " + productId));

        product.likeCountUp();
    }

    @Transactional
    public void unlikeProduct(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. userId: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId: " + productId));

        Optional<ProductLike> byUserAndProductOptional =
                productLikeRepository.findByUserIdAndProductId(user.getId(), product.getId());
        if (byUserAndProductOptional.isEmpty()) {
            return;
        }

        productLikeRepository.delete(byUserAndProductOptional.get());

        applicationEventPublisher.publishEvent(new ProductUnlikedEvent(userId, productId));
    }

    @Transactional
    public void unlikeCountDown(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "상품이 존재하지 않습니다. productId: " + productId));

        product.unlikeCountDown();
    }
}
