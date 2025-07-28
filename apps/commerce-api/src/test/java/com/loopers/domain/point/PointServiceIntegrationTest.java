package com.loopers.domain.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.data.UserCreateData;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class PointServiceIntegrationTest {
    @Autowired
    private PointFacade pointFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private PointRepository pointRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Create {
        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void failsWhenChargingPointsToNonExistentUser() {
            // arrange
            String userId = "user1234";
            Integer point = 10_000;

            // act
            // assert
            assertThatThrownBy(() -> pointFacade.charge(userId, point))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @DisplayName("포인트를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsUserPointsWhenUserExists() {
            // arrange
            String userId = "user1234";
            UserCreateData userCreateData = new UserCreateData(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateData));

            pointRepository.save(Point.create(userId, 10_000));

            // act
            PointInfo pointInfo = pointFacade.get(userId);

            // assert
            assertAll(
                () -> assertThat(pointInfo).isNotNull(),
                () -> assertThat(pointInfo.id()).isNotNull(),
                () -> assertThat(pointInfo.userId()).isEqualTo(userCreateData.userId()),
                () -> assertThat(pointInfo.amount()).isEqualTo(10_000)
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNullWhenUserDoesNotExist() {
            // arrange
            String userId = "invalidUserId";

            // act
            PointInfo pointInfo = pointFacade.get(userId);

            // assert
            assertThat(pointInfo).isNull();
        }
    }
}
