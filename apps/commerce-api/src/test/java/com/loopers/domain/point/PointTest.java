package com.loopers.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {
    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Create {
        @ValueSource(longs = {99L, -1L, -1000L})
        @ParameterizedTest
        @DisplayName("100 미만의 정수로 포인트를 충전 시 실패한다.")
        void failsWhenChargingWithZeroOrNegativePoints(Long amount) {
            // arrange
            Long userId = 1L;
            Point point = Point.create(userId);

            // assert
            assertThatThrownBy(() -> point.charge(amount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
