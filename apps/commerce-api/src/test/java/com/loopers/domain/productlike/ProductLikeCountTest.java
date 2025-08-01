package com.loopers.domain.productlike;

import com.loopers.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductLikeCountTest {
    @Mock
    private Product product;

    @Test
    @DisplayName("0 이하의 좋아요 수를 감소시키려고 할 때 예외가 발생한다.")
    void decreaseBelowZero() {
        // arrange
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);

        // act
        // assert
        assertThatThrownBy(productLikeCount::decrease)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("상품 좋아요 수를 생성할 때, 상품 좋아요 수가 0으로 초기화된다.")
    void likeSuccess() {
        // act
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);

        // assert
        assertThat(productLikeCount.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 좋아요 수를 증가시킬 때, 좋아요 수가 0인 경우 1 증가하여 좋아요 수가 1이 되어야 한다")
    void likeIncrease() {
        // arrange
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);
        assertThat(productLikeCount.getLikeCount()).isEqualTo(0);

        // act
        productLikeCount.increase();

        // assert
        assertThat(productLikeCount.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품 좋아요 수를 증가시킬 때, 좋아요를 여러 번 증가시키면 호출 횟수만큼 좋아요 수가 증가해야 한다")
    void likeMultipleIncrease() {
        // arrange
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);
        assertThat(productLikeCount.getLikeCount()).isEqualTo(0);

        // act
        productLikeCount.increase();
        productLikeCount.increase();
        productLikeCount.increase();

        // assert
        assertThat(productLikeCount.getLikeCount()).isEqualTo(3);
    }


    @Test
    @DisplayName("상품 좋아요 수를 감소시킬 때, 좋아요 수가 1인 경우 1 감소하여 좋아요 수가 0이 되어야 한다")
    void likeDecrease() {
        // arrange
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);
        productLikeCount.increase();

        assertThat(productLikeCount.getLikeCount()).isEqualTo(1);

        // act
        productLikeCount.decrease();

        // assert
        assertThat(productLikeCount.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 좋아요 수를 감소시킬 때, 좋아요를 여러 번 감소시키면 호출 횟수만큼 좋아요 수가 감소해야 한다")
    void likeMultipleDecrease() {
        // arrange
        ProductLikeCount productLikeCount = ProductLikeCount.create(product);
        productLikeCount.increase();
        productLikeCount.increase();
        productLikeCount.increase();

        assertThat(productLikeCount.getLikeCount()).isEqualTo(3);

        // act
        productLikeCount.decrease();
        productLikeCount.decrease();

        // assert
        assertThat(productLikeCount.getLikeCount()).isEqualTo(1);
    }
}
