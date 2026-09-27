package com.loopers.domain.user;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.dto.command.UserCreateInfo;
import com.loopers.domain.user.type.GenderType;
import com.loopers.support.converter.DateConverter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "member")
@Entity
public class User extends BaseEntity {

    private static final String VALID_EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    private String name;

    private String email;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    private User(UserCreateInfo userCreateInfo) {
        validateEmailFormat(userCreateInfo.email());
        LocalDate birthDateTime = convertBirthDateFormat(userCreateInfo.birthDateString());

        this.name = userCreateInfo.name();
        this.email = userCreateInfo.email();
        this.birthDate = birthDateTime;
    }

    public static User create(UserCreateInfo userCreateInfo) {
        return new User(userCreateInfo);
    }

    private void validateEmailFormat(String email) {
        validateEmailPattern(email);
    }

    private void validateEmailPattern(String email) {
        if (!email.matches(VALID_EMAIL_PATTERN)) {
            throw new IllegalArgumentException("이메일이 xx@yy.zz 형식에 맞지 않습니다.");
        }
    }

    private LocalDate convertBirthDateFormat(String birthDateString) {
        try {
            return DateConverter.convertToLocalDate(birthDateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일이 yyyy-MM-dd 형식에 맞지 않습니다.");
        }
    }
}
