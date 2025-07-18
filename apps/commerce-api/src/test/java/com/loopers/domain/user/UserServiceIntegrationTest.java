package com.loopers.domain.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.user.dto.data.UserCreateData;
import com.loopers.domain.user.type.GenderType;
import com.loopers.support.validation.DateConverter;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    private UserFacade userFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    @MockitoSpyBean
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("유저를 생성할 때, ")
    @Nested
    class Create {
        @DisplayName("회원 가입시 User 저장이 수행된다.")
        @Test
        void successfullySavesUserOnRegistration() {
            // arrange
            String userId = "user1234";
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateData userCreateData = new UserCreateData(userId, name, email, birthDateString, gender);

            // act
            UserInfo userInfo = userFacade.create(userCreateData);

            // assert
            assertAll(
                () -> assertThat(userInfo).isNotNull(),
                () -> assertThat(userInfo.id()).isNotNull(),
                () -> assertThat(userInfo.userId()).isEqualTo(userCreateData.userId()),
                () -> assertThat(userInfo.name()).isEqualTo(userCreateData.name()),
                () -> assertThat(userInfo.email()).isEqualTo(userCreateData.email()),
                () -> assertThat(userInfo.birthDate()).isEqualTo(DateConverter.convertToLocalDate(userCreateData.birthDateString()))
            );

            verify(userRepository, times(1)).save(any(User.class));
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void failsWhenRegisteringWithExistingUserId() {
            // arrange
            String userId = "user1234";
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateData userCreateData = new UserCreateData(userId, name, email, birthDateString, gender);
            userFacade.create(userCreateData);

            // act
            // assert
            assertThatThrownBy(() -> userFacade.create(userCreateData))
                    .isInstanceOf(Exception.class); // exception 뭘로 만들 지 고민해보기
        }
    }

    @DisplayName("유저를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnsUserInfoWhenUserExists() {
            // arrange
            String userId = "user1234";
            UserCreateData userCreateData = new UserCreateData(
                    userId,
                    "park",
                    "user@domain.com",
                    "2000-01-01",
                    MALE);
            userRepository.save(User.create(userCreateData));

            // act
            UserInfo userInfo = userFacade.get(userId);

            // assert
            assertAll(
                    () -> assertThat(userInfo).isNotNull(),
                    () -> assertThat(userInfo.id()).isNotNull(),
                    () -> assertThat(userInfo.userId()).isEqualTo(userCreateData.userId()),
                    () -> assertThat(userInfo.name()).isEqualTo(userCreateData.name()),
                    () -> assertThat(userInfo.email()).isEqualTo(userCreateData.email()),
                    () -> assertThat(userInfo.birthDate()).isEqualTo(DateConverter.convertToLocalDate(userCreateData.birthDateString()))
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNullWhenUserDoesNotExist() {
            // arrange
            String userId = "invalidUserId";

            // act
            UserInfo userInfo = userFacade.get(userId);

            // assert
            assertThat(userInfo).isNull();
        }
    }
}
