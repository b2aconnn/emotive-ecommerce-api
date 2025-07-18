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
        @ValueSource(ints = {0, -1, -1000})
        @ParameterizedTest
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        void failsWhenChargingWithZeroOrNegativePoints(Integer point) {
            // act
            String userId = "abcd1234";

            // assert
            assertThatThrownBy(() -> Point.create(userId, point))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
