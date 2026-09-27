package com.loopers.application.user.dto;

import java.time.LocalDate;

import com.loopers.domain.user.User;

public record UserResult(Long id, String name, String email, LocalDate birthDate) {
    public static UserResult from(User user) {
        return new UserResult(user.getId(), user.getName(), user.getEmail(), user.getBirthDate());
    }
}
