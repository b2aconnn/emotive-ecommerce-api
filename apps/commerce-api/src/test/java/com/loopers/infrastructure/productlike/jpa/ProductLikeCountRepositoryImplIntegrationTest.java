package com.loopers.infrastructure.productlike.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.productlike.ProductLikeCountRepository;
import com.loopers.domain.productlike.dto.result.ProductLikeCountResult;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.fixture.product.ProductWithStockFixture;
import com.loopers.fixture.productlike.ProductLikeCountFixture;
import com.loopers.utils.DatabaseCleanUp;

@SpringBootTest
class ProductLikeCountRepositoryImplIntegrationTest {

    @Autowired
    private ProductLikeCountRepository productLikeCountRepository;

    @Autowired
    private ProductLikeCountFixture productLikeCountFixture;

    @Autowired
    private BrandFixture brandFixture;

    @Autowired
    private ProductWithStockFixture productWithStockFixture;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Product product;
    private Product anotherProduct;

    @BeforeEach
    void setUp() {
        Brand brand = brandFixture.save();
        product = productWithStockFixture.save(brand, 1_000L, 100L);
        anotherProduct = productWithStockFixture.save(brand, 2_000L, 100L);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("여러 상품의 좋아요 수를 한 번에 조회할 때, ")
    @Nested
    class FindLikeCountsByProductIds {
        @DisplayName("상품별 현재 좋아요 수를 반환한다.")
        @Test
        void returnsLikeCountPerProduct() {
            // given
            productLikeCountFixture.save(product, 3);
            productLikeCountFixture.save(anotherProduct, 5);

            // when
            List<ProductLikeCountResult> results = productLikeCountRepository.findLikeCountsByProductIds(
                    List.of(product.getId(), anotherProduct.getId()));

            // then
            assertThat(results)
                    .extracting(ProductLikeCountResult::productId, ProductLikeCountResult::likeCount)
                    .containsExactlyInAnyOrder(tuple(product.getId(), 3L), tuple(anotherProduct.getId(), 5L));
        }

        @DisplayName("좋아요 수 행이 없는 상품은 결과에 포함되지 않는다. (호출자가 0으로 처리한다)")
        @Test
        void excludesProductWithoutLikeCountRow() {
            // given
            productLikeCountFixture.save(product, 3);

            // when
            List<ProductLikeCountResult> results = productLikeCountRepository.findLikeCountsByProductIds(
                    List.of(product.getId(), anotherProduct.getId()));

            // then
            assertThat(results).extracting(ProductLikeCountResult::productId).containsExactly(product.getId());
        }

        @DisplayName("상품 목록이 비어 있으면, 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyListWhenProductIdsIsEmpty() {
            // given
            productLikeCountFixture.save(product, 3);

            // when
            List<ProductLikeCountResult> results = productLikeCountRepository.findLikeCountsByProductIds(List.of());

            // then
            assertThat(results).isEmpty();
        }
    }
}
