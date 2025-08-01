package com.loopers.application.productlike;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.productlike.ProductLikeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductLikeAppService {
    private final ProductLikeService productLikeService;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void likeProduct(String userId, Long productId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. userId: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId: " + productId));

        productLikeService.likeProduct(user, product);
    }

    @Transactional
    public void unlikeProduct(String userId, Long productId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. userId: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. productId: " + productId));

        productLikeService.unlikeProduct(user, product);
    }
}
