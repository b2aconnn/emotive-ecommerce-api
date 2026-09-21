package com.loopers.infrastructure.order.jpa;

import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.dto.result.UserOrderCountResult;
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
import static com.loopers.domain.order.OrderStatus.CREATED;
import static com.loopers.domain.order.OrderStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
class OrderRepositoryImplIntegrationTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final ZonedDateTime PERIOD_START = LocalDate.of(2026, 7, 6).atStartOfDay(ZONE);
    private static final ZonedDateTime PERIOD_END_EXCLUSIVE = LocalDate.of(2026, 7, 7).atStartOfDay(ZONE);

    @Autowired
    private OrderRepository orderRepository;

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

    @BeforeEach
    void setUp() {
        product = productWithStockFixture.save(brandFixture.save(), 1_000L, 100L);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("기간 내 완료 주문을 사용자별로 집계할 때, ")
    @Nested
    class CountCompletedOrdersGroupByUser {
        @DisplayName("사용자별 완료 주문 건수를 반환한다.")
        @Test
        void countsCompletedOrdersPerUser() {
            // given
            User buyer = userFixture.save();
            User anotherBuyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), product);
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(2), product);
            completedOrderFixture.save(anotherBuyer.getId(), PERIOD_START.plusHours(3), product);

            // when
            List<UserOrderCountResult> results =
                    orderRepository.countCompletedOrdersGroupByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results)
                    .extracting(UserOrderCountResult::userId, UserOrderCountResult::orderCount)
                    .containsExactlyInAnyOrder(
                            tuple(buyer.getId(), 2L),
                            tuple(anotherBuyer.getId(), 1L)
                    );
        }

        @DisplayName("기간 시작 시각에 정확히 생성된 주문은 포함하고, 기간 종료 시각에 생성된 주문은 제외한다. ([start, end) 반개구간)")
        @Test
        void appliesHalfOpenIntervalOnPeriodBoundary() {
            // given
            User onStart = userFixture.save();
            User onEnd = userFixture.save();
            User justBeforeEnd = userFixture.save();
            User beforeStart = userFixture.save();

            completedOrderFixture.save(onStart.getId(), PERIOD_START, product);
            completedOrderFixture.save(onEnd.getId(), PERIOD_END_EXCLUSIVE, product);
            completedOrderFixture.save(justBeforeEnd.getId(), PERIOD_END_EXCLUSIVE.minusNanos(1_000_000), product);
            completedOrderFixture.save(beforeStart.getId(), PERIOD_START.minusNanos(1_000_000), product);

            // when
            List<UserOrderCountResult> results =
                    orderRepository.countCompletedOrdersGroupByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).extracting(UserOrderCountResult::userId)
                    .containsExactlyInAnyOrder(onStart.getId(), justBeforeEnd.getId());
        }

        @DisplayName("COMPLETED 가 아닌 주문은 집계에 포함하지 않는다.")
        @Test
        void excludesOrdersNotCompleted() {
            // given
            User buyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), product);
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(2), CREATED, product);
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(3), PENDING, product);
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(4), CANCELED, product);

            // when
            List<UserOrderCountResult> results =
                    orderRepository.countCompletedOrdersGroupByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).singleElement()
                    .satisfies(result -> assertThat(result.orderCount()).isEqualTo(1L));
        }

        @DisplayName("기간 내 완료 주문이 하나도 없으면, 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyListWhenNoCompletedOrderExists() {
            // given
            User buyer = userFixture.save();
            completedOrderFixture.save(buyer.getId(), PERIOD_START.plusHours(1), CANCELED, product);

            // when
            List<UserOrderCountResult> results =
                    orderRepository.countCompletedOrdersGroupByUser(PERIOD_START, PERIOD_END_EXCLUSIVE);

            // then
            assertThat(results).isEmpty();
        }
    }
}
