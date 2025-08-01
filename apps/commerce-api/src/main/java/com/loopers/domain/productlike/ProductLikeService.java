package com.loopers.domain.productlike;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductLikeService {
    private final ProductLikeRepository productLikeRepository;
    private final ProductLikeCountRepository productLikeCountRepository;

    public void likeProduct(User user, Product product) {
        if (productLikeRepository.hasUserLikedProduct(user.getId(), product.getId())) {
            return;
        }

        productLikeRepository.save(ProductLike.create(user, product));

        ProductLikeCount productLikeCount = productLikeCountRepository.findByProductId(product.getId())
                .orElse(ProductLikeCount.create(product));
        productLikeCount.increase();
        productLikeCountRepository.save(productLikeCount);
    }

    public void unlikeProduct(User user, Product product) {
        Optional<ProductLike> byUserAndProductOptional =
                productLikeRepository.findByUserAndProduct(user.getId(), product.getId());
        if (byUserAndProductOptional.isEmpty()) {
            return;
        }
        productLikeRepository.delete(byUserAndProductOptional.get());

        ProductLikeCount productLikeCount = productLikeCountRepository.findByProductId(product.getId())
                .orElseThrow(() -> new IllegalStateException("상품 좋아요 카운트가 존재하지 않습니다."));
        productLikeCount.decrease();
    }
}
