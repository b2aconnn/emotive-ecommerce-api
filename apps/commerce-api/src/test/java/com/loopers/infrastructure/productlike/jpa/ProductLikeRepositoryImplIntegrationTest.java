package com.loopers.infrastructure.productlike.jpa;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.productlike.ProductLike;
import com.loopers.domain.productlike.ProductLikeRepository;
import com.loopers.domain.productlike.dto.result.UserLikedProductResult;
import com.loopers.domain.user.User;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.fixture.product.ProductWithStockFixture;
import com.loopers.fixture.productlike.ProductLikeFixture;
import com.loopers.fixture.user.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class ProductLikeRepositoryImplIntegrationTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final ZonedDateTime PERIOD_START = LocalDate.of(2026, 7, 6).atStartOfDay(ZONE);
    private static final ZonedDateTime PERIOD_END_EXCLUSIVE = LocalDate.of(2026, 7, 7).atStartOfDay(ZONE);

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductLikeFixture productLikeFixture;

    @Autowired
    private UserFixture userFixture;

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

    @DisplayName("기간 내 좋아요의 (사용자, 상품) 쌍을 조회할 때, ")
    @Nested
    class FindLikedProductsByUserInPeriod {
        @DisplayName("기간 내에 눌린 좋아요를 사용자/상품 쌍으로 반환한다.")
        @Test
        void returnsLikedProductsPerUser() {
            // given
            User user = userFixture.save();
            User anotherUser = userFixture.save();
            productLikeFixture.save(user, product, PERIOD_START.plusHours(1));
            productLikeFixture.save(user, anotherProduct, PERIOD_START.plusHours(2));
            productLikeFixture.save(anotherUser, product, PERIOD_START.plusHours(3));

            // when
            List<UserLikedProductResult> results =
                    productLikeRepository.findLikedProductsByUserInPeriod(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results)
                    .extracting(UserLikedProductResult::userId, UserLikedProductResult::productId)
                    .containsExactlyInAnyOrder(
                            tuple(user.getId(), product.getId()),
                            tuple(user.getId(), anotherProduct.getId()),
                            tuple(anotherUser.getId(), product.getId())
                    );
        }

        @DisplayName("기간 중에 눌렀다가 취소(하드 삭제)한 좋아요는, 집계 시점에 행이 없으므로 조회되지 않는다.")
        @Test
        void excludesCanceledLike() {
            // given
            User user = userFixture.save();
            productLikeFixture.save(user, product, PERIOD_START.plusHours(1));
            ProductLike canceledLike = productLikeFixture.save(user, anotherProduct, PERIOD_START.plusHours(2));

            // when
            productLikeRepository.delete(canceledLike);
            List<UserLikedProductResult> results =
                    productLikeRepository.findLikedProductsByUserInPeriod(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).containsExactly(new UserLikedProductResult(user.getId(), product.getId()));
        }

        @DisplayName("기간 시작 시각의 좋아요는 포함하고, 기간 종료 시각의 좋아요는 제외한다. ([start, end) 반개구간)")
        @Test
        void appliesHalfOpenIntervalOnPeriodBoundary() {
            // given
            User user = userFixture.save();
            User onEndUser = userFixture.save();
            User beforeStartUser = userFixture.save();
            productLikeFixture.save(user, product, PERIOD_START);
            productLikeFixture.save(onEndUser, product, PERIOD_END_EXCLUSIVE);
            productLikeFixture.save(beforeStartUser, product, PERIOD_START.minusNanos(1_000_000));

            // when
            List<UserLikedProductResult> results =
                    productLikeRepository.findLikedProductsByUserInPeriod(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).containsExactly(new UserLikedProductResult(user.getId(), product.getId()));
        }

        @DisplayName("기간 내 좋아요가 없으면, 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyListWhenNoLikeExistsInPeriod() {
            // given
            User user = userFixture.save();
            productLikeFixture.save(user, product, PERIOD_END_EXCLUSIVE.plusDays(1));

            // when
            List<UserLikedProductResult> results =
                    productLikeRepository.findLikedProductsByUserInPeriod(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).isEmpty();
        }
    }
}
