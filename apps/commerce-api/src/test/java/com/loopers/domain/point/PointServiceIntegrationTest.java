package com.loopers.domain.point;

import com.loopers.application.point.PointAppService;
import com.loopers.application.point.dto.PointInfo;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityNotFoundException;
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
    private PointAppService pointAppService;

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
            Long point = 10_000L;

            // act
            // assert
            assertThatThrownBy(() -> pointAppService.charge(userId, point))
                    .isInstanceOf(EntityNotFoundException.class);
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
            UserCreateInfo userCreateInfo = new UserCreateInfo(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            Point point = Point.create(saveUser.getId());
            point.charge(10_000L);
            pointRepository.save(point);

            // act
            PointInfo pointInfo = pointAppService.get(userId);

            // assert
            assertAll(
                () -> assertThat(pointInfo).isNotNull(),
                () -> assertThat(pointInfo.id()).isNotNull(),
                () -> assertThat(pointInfo.userId()).isEqualTo(userCreateInfo.userId()),
                () -> assertThat(pointInfo.amount()).isEqualTo(10_000)
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNullWhenUserDoesNotExist() {
            // arrange
            String userId = "invalidUserId";

            // act
            PointInfo pointInfo = pointAppService.get(userId);

            // assert
            assertThat(pointInfo).isNull();
        }
    }
}
