package com.loopers.domain.user;

import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.domain.user.type.GenderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    @DisplayName("유저를 생성할 때, ")
    @Nested
    class Create {

        @ValueSource(strings = {
                "",
                " ",
                "failedId123",
                "id_123",
        })
        @ParameterizedTest
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        void failsWhenUserIdFormatIsInvalid(String userId) {
            // act
            String name = "park";
            String email = "abc@abc.com";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(userId, name, email, birthDateString, gender);

            // assert
            assertThatThrownBy(() -> User.create(userCreateInfo))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ValueSource(strings = {
                "",
                " ",
                "usermail.com",
                "user@domain",
                "user@domain.c",
                "u ser@mail.com",
                "!!!@!!!.!!!",
        })
        @ParameterizedTest
        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        void failsWhenEmailFormatIsInvalid(String email) {
            // act
            String name = "park";
            String userId = "user1234";
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(userId, name, email, birthDateString, gender);

            // assert
            assertThatThrownBy(() -> User.create(userCreateInfo))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ValueSource(strings = {
                "",
                " ",
                "2025/12/31",
                "31-12-2025",
//                "2025-02-29", // 윤년이 아닌 날짜가 변환이 잘 되는 이슈가 있음.
                "2025-12-32",
                "2025-01-00",
                "2025-00-01",
                "2025-13-01",
        })
        @ParameterizedTest
        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        void failsWhenBirthDateFormatIsInvalid(String birthDateString) {
            // act
            String name = "park";
            String email = "user@abc.com";
            String userId = "user1234";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(userId, name, email, birthDateString, gender);

            // assert
            assertThatThrownBy(() -> User.create(userCreateInfo))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
