package com.loopers.domain.user;

import static com.loopers.domain.user.type.GenderType.MALE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.domain.user.type.GenderType;

class UserTest {
    @DisplayName("유저를 생성할 때, ")
    @Nested
    class Create {

        @ValueSource(
                strings = {
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
            String birthDateString = "2000-01-01";
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(name, email, birthDateString, gender);

            // assert
            assertThatThrownBy(() -> User.create(userCreateInfo)).isInstanceOf(IllegalArgumentException.class);
        }

        @ValueSource(
                strings = {
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
            GenderType gender = MALE;
            UserCreateInfo userCreateInfo = new UserCreateInfo(name, email, birthDateString, gender);

            // assert
            assertThatThrownBy(() -> User.create(userCreateInfo)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
