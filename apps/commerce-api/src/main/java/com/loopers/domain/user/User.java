package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.dto.command.UserCreateCommand;
import com.loopers.domain.user.type.GenderType;
import com.loopers.support.converter.DateConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "member")
@Entity
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String userId;

    private String name;

    private String email;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    private final String VALID_USER_ID_PATTERN = "^[A-Za-z0-9]{1,10}$";
    private final String VALID_EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    @Builder
    private User(String userId, String name, String email, String birthDateString) {
        validateIdFormat(userId);
        validateEmailFormat(email);
        LocalDate birthDateTime = convertBirthDateFormat(birthDateString);

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.birthDate = birthDateTime;
    }

    public static User create(UserCreateCommand userCreateCommand) {
        return User.builder()
                .userId(userCreateCommand.userId())
                .name(userCreateCommand.name())
                .email(userCreateCommand.email())
                .birthDateString(userCreateCommand.birthDateString())
                .build();
    }

    private void validateIdFormat(String id) {
        validateIdPattern(id);
    }

    private void validateIdPattern(String id) {
        if (id.length() > 10 || !id.matches(VALID_USER_ID_PATTERN)) {
            throw new IllegalArgumentException("영문 및 숫자 10자 이내로 입력해주세요.");
        }
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
