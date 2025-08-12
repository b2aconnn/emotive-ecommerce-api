package com.loopers.domain.order;

import com.loopers.application.order.OrderAppService;
import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.application.order.dto.OrderLineItem;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.fixture.infra.brand.BrandFixture;
import com.loopers.fixture.point.PointFixture;
import com.loopers.fixture.product.ProductWithStockFixture;
import com.loopers.fixture.user.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderAppServiceIntegrationTest {
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private PointFixture pointFixture;

    @Autowired
    private BrandFixture brandFixture;

    @Autowired
    private ProductWithStockFixture productWithStockFixture;

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문을 완료할 때, ")
    @Nested
    class POST {
        @DisplayName("현재 보유 포인트가 100원이고 100원의 상품 1개를 구매할 때, 포인트 100원을 사용해 주문 완료된다.")
        @Test
        void completeOrderWithFullPoints() {
            // given
            User user = userFixture.save();

            Long balance = 100L;
            pointFixture.save(user.getId(), balance);

            Long productPrice = 100L;
            Integer stockQuantity = 1;
            Product saveProduct = productWithStockFixture.save(
                    brandFixture.save(),
                    productPrice,
                    stockQuantity);

            // when
            Order order = orderAppService.order(new OrderCreateCommand(
                    user.getId(),
                    "주문자",
                    "seoul gangnam",
                    "010-0000-0000",
                    List.of(new OrderLineItem(saveProduct.getId(), 1L)),
                    100L));

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            Optional<Point> pointOptional = pointRepository.findByUserId(user.getId());
            assertThat(pointOptional.isPresent()).isTrue();
            assertThat(pointOptional.get().getBalance()).isEqualTo(0);
        }
    }
}
