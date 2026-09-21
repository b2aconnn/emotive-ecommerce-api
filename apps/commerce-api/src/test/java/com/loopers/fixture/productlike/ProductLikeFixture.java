package com.loopers.fixture.productlike;

import com.loopers.domain.product.Product;
import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.fixture.support.CreatedAtBackdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/** 원하는 생성 시각의 좋아요를 저장한다. 취소는 하드 삭제이므로 {@link ProductLikeRepository#delete}를 그대로 쓴다. */
@Component
public class ProductLikeFixture {

    private static final String PRODUCT_LIKE_TABLE = "product_like";

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private CreatedAtBackdater createdAtBackdater;

    public ProductLike save(User user, Product product, ZonedDateTime createdAt) {
        ProductLike savedProductLike = productLikeRepository.save(ProductLike.create(user, product));
        createdAtBackdater.backdate(PRODUCT_LIKE_TABLE, savedProductLike.getId(), createdAt);
        return savedProductLike;
    }
}
