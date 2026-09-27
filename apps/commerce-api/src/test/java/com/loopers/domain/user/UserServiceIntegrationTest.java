package com.loopers.domain.user;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.loopers.application.user.UserService;
import com.loopers.application.user.dto.UserResult;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.domain.user.type.GenderType;
import com.loopers.utils.DatabaseCleanUp;

@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

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
            String name = "park";
            String email = "user@domain.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(name, email, birthDateString, gender);

            // act
            UserResult userInfo = userService.create(userCreateInfo);

            // assert
            assertAll(
                    () -> assertThat(userInfo).isNotNull(),
                    () -> assertThat(userInfo.id()).isNotNull(),
                    () -> assertThat(userInfo.name()).isEqualTo(userCreateInfo.name()),
                    () -> assertThat(userInfo.email()).isEqualTo(userCreateInfo.email()),
                    () -> assertThat(userInfo.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1)));

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @DisplayName("유저를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnsUserInfoWhenUserExists() {
            // arrange
            UserCreateInfo userCreateInfo = new UserCreateInfo("park", "user@domain.com", "2000-01-01", MALE);
            User saveUser = userRepository.save(User.create(userCreateInfo));

            // act
            UserResult userInfo = userService.get(saveUser.getId());

            // assert
            assertAll(
                    () -> assertThat(userInfo).isNotNull(),
                    () -> assertThat(userInfo.id()).isEqualTo(saveUser.getId()),
                    () -> assertThat(userInfo.name()).isEqualTo(userCreateInfo.name()),
                    () -> assertThat(userInfo.email()).isEqualTo(userCreateInfo.email()),
                    () -> assertThat(userInfo.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1)));
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 예외가 발생한다.")
        @Test
        void throwsExceptionWhenUserDoesNotExist() {
            // act
            // assert
            assertThatThrownBy(() -> userService.get(999L)).isInstanceOf(EntityNotFoundException.class);
        }
    }
}
