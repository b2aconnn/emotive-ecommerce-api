package com.loopers.domain.point;

import com.loopers.application.point.PointService;
import com.loopers.application.point.dto.PointResult;
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
    private PointService pointService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Create {
        @DisplayName("포인트가 존재하지 않는 유저라면, 새로 생성해서 충전한다.")
        @Test
        void createsNewPointWhenChargingForTheFirstTime() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateInfo(
                    "park", "user@domain.com", "2000-01-01", MALE)));
            Long amount = 10_000L;

            // act
            PointResult result = pointService.charge(saveUser.getId(), amount);

            // assert
            assertAll(
                () -> assertThat(result.userId()).isEqualTo(saveUser.getId()),
                () -> assertThat(result.amount()).isEqualTo(amount)
            );
        }
    }

    @DisplayName("포인트를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 포인트가 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsUserPointsWhenUserExists() {
            // arrange
            User saveUser = userRepository.save(User.create(new UserCreateInfo(
                    "park", "user@domain.com", "2000-01-01", MALE)));

            Point point = Point.create(saveUser.getId());
            point.charge(10_000L);
            pointRepository.save(point);

            // act
            PointResult pointInfo = pointService.get(saveUser.getId());

            // assert
            assertAll(
                () -> assertThat(pointInfo).isNotNull(),
                () -> assertThat(pointInfo.id()).isNotNull(),
                () -> assertThat(pointInfo.userId()).isEqualTo(saveUser.getId()),
                () -> assertThat(pointInfo.amount()).isEqualTo(10_000)
            );
        }

        @DisplayName("해당 ID 의 포인트 정보가 없을 경우, 예외가 발생한다.")
        @Test
        void throwsWhenPointDoesNotExist() {
            // act
            // assert
            assertThatThrownBy(() -> pointService.get(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
