package com.loopers.fixture.productlike;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.loopers.domain.product.Product;
import com.loopers.domain.productlike.ProductLikeCount;
import com.loopers.domain.productlike.ProductLikeCountRepository;

/** 상품의 현재 좋아요 수 행을 만든다. {@code likeCount}는 0부터 시작하므로 원하는 만큼 증가시킨다. */
@Component
public class ProductLikeCountFixture {

    @Autowired
    private ProductLikeCountRepository productLikeCountRepository;

    public ProductLikeCount save(Product product, int likeCount) {
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);
        for (int i = 0; i < likeCount; i++) {
            productLikeCount.increase();
        }

        return productLikeCountRepository.save(productLikeCount);
    }
}
