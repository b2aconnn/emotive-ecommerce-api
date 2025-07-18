package com.loopers.application.user;

import com.loopers.domain.user.User;

import java.time.LocalDate;

public record UserInfo(Long id,
                       String userId,
                       String name,
                       String email,
                       LocalDate birthDate) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate()
        );
    }
}
