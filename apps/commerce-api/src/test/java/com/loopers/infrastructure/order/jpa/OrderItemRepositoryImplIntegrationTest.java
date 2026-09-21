package com.loopers.infrastructure.order.jpa;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.dto.result.UserOrderedProductResult;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.fixture.order.CompletedOrderFixture;
import com.loopers.fixture.product.ProductWithStockFixture;
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

import static com.loopers.domain.order.OrderStatus.CANCELED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class OrderItemRepositoryImplIntegrationTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final ZonedDateTime PERIOD_START = LocalDate.of(2026, 7, 6).atStartOfDay(ZONE);
    private static final ZonedDateTime PERIOD_END_EXCLUSIVE = LocalDate.of(2026, 7, 7).atStartOfDay(ZONE);

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CompletedOrderFixture completedOrderFixture;

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

    @DisplayName("기간 내 완료 주문의 (사용자, 상품) 쌍을 조회할 때, ")
    @Nested
    class FindDistinctCompletedOrderedProductsByUser {
        @DisplayName("같은 사용자가 같은 상품을 여러 주문에 걸쳐 샀어도, 상품 기준으로 중복 제거되어 1건만 조회된다.")
        @Test
        void deduplicatesSameProductAcrossMultipleOrders() {
            // given
            User buyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), product);
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(2), product);

            // when
            List<UserOrderedProductResult> results = orderItemRepository
                    .findDistinctCompletedOrderedProductsByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).containsExactly(new UserOrderedProductResult(buyer.getId(), product.getId()));
        }

        @DisplayName("같은 주문에 같은 상품이 여러 라인으로 담겨 있어도, 1건으로 중복 제거된다.")
        @Test
        void deduplicatesSameProductWithinOneOrder() {
            // given
            User buyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), product, product);

            // when
            List<UserOrderedProductResult> results = orderItemRepository
                    .findDistinctCompletedOrderedProductsByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).containsExactly(new UserOrderedProductResult(buyer.getId(), product.getId()));
        }

        @DisplayName("사용자별로 주문한 상품이 각각 조회된다.")
        @Test
        void returnsOrderedProductsPerUser() {
            // given
            User buyer = userFixture.save();
            User anotherBuyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), product, anotherProduct);
            completedOrderFixture.save(anotherBuyer.getId(), PERIOD_START.plusHours(2), anotherProduct);

            // when
            List<UserOrderedProductResult> results = orderItemRepository
                    .findDistinctCompletedOrderedProductsByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results)
                    .extracting(UserOrderedProductResult::userId, UserOrderedProductResult::productId)
                    .containsExactlyInAnyOrder(
                            tuple(buyer.getId(), product.getId()),
                            tuple(buyer.getId(), anotherProduct.getId()),
                            tuple(anotherBuyer.getId(), anotherProduct.getId())
                    );
        }

        @DisplayName("기간은 주문의 생성 시각을 기준으로 판단하며, [start, end) 반개구간을 따른다.")
        @Test
        void appliesHalfOpenIntervalOnOrderCreatedAt() {
            // given
            User onStart = userFixture.save();
            User onEnd = userFixture.save();
            completedOrderFixture.save(onStart.getId(), PERIOD_START, product);
            completedOrderFixture.save(onEnd.getId(), PERIOD_END_EXCLUSIVE, anotherProduct);

            // when
            List<UserOrderedProductResult> results = orderItemRepository
                    .findDistinctCompletedOrderedProductsByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).containsExactly(new UserOrderedProductResult(onStart.getId(), product.getId()));
        }

        @DisplayName("COMPLETED 가 아닌 주문의 상품은 조회되지 않는다.")
        @Test
        void excludesProductsOfOrdersNotCompleted() {
            // given
            User buyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), CANCELED, product);

            // when
            List<UserOrderedProductResult> results = orderItemRepository
                    .findDistinctCompletedOrderedProductsByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).isEmpty();
        }
    }
}
