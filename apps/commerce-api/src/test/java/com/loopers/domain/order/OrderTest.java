package com.loopers.domain.order;

import com.loopers.domain.order.dto.OrderCreateInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.loopers.domain.order.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {
    @DisplayName("주문을 생성할 때, ")
    @Nested
    class Create {
        @DisplayName("주문 상품이 없는 경우 주문 생성이 실패한다.")
        @Test
        void createOrderWithoutProducts() {
            // act
            // assert
            assertThatThrownBy(() -> Order.create(new OrderCreateInfo(
                    1L,
                    "홍길동",
                    "서울시 강남구",
                    "010-1234-5678",
                    List.of())))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("주문한 사용자 정보가 누락된 경우 주문 생성이 실패한다.")
        @Test
        void createOrderWithMissingUserInfo() {
            // act
            // assert
            assertThatThrownBy(() -> Order.create(new OrderCreateInfo(
                    null,
                    "홍길동",
                    "서울시 강남구",
                    "010-1234-5678",
                    List.of())))
                    .isInstanceOf(NullPointerException.class);
        }

        private static Stream<Arguments> invalidOrderParameters() {
            return Stream.of(
                    Arguments.of(1L, null, "서울시 강남구", "010-1234-5678"),
                    Arguments.of(1L, " ", "서울시 강남구", "010-1234-5678"),
                    Arguments.of(1L, "홍길동", null, "010-1234-5678"),
                    Arguments.of(1L, "홍길동", " ", "010-1234-5678"),
                    Arguments.of(1L, "홍길동", "서울시 강남구", null),
                    Arguments.of(1L, "홍길동", "서울시 강남구", " ")
            );
        }

        @DisplayName("주문자명, 배송지, 주문자 연락처 정보가 누락된 경우 주문 생성이 실패한다.")
        @MethodSource("invalidOrderParameters")
        @ParameterizedTest
        void createOrderWithMissingUserInfo(Long userId, String userName, String deliveryAddress, String contactNumber) {
            // arrange
            List<OrderItem> orderItems = List.of(new OrderItem());

            // act
            // assert
            assertThatThrownBy(() -> Order.create(new OrderCreateInfo(
                    userId,
                    userName,
                    deliveryAddress,
                    contactNumber,
                    orderItems
            ))).isInstanceOf(IllegalArgumentException.class);
        }

        private static Stream<Arguments> validOrderParameters() {
            return Stream.of(
                    Arguments.of(1L, "홍길동", "서울시 강남구", "010-1234-5678"),
                    Arguments.of(2L, "김철수", "부산시 해운대구", "010-9876-5432")
            );
        }

        @DisplayName("주문 항목이 비어있지 않은 경우 주문 생성된다.")
        @MethodSource("validOrderParameters")
        @ParameterizedTest
        void createOrderWithValidParameters(Long userId, String userName, String deliveryAddress, String contactNumber) {
            // act
            Order order = Order.create(new OrderCreateInfo(
                    userId,
                    userName,
                    deliveryAddress,
                    contactNumber,
                    List.of(new OrderItem())
            ));

            // assert
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(CREATED);
        }
    }
}
