package com.loopers.interfaces.api.user.dto;

import com.loopers.application.user.dto.UserResult;

import java.time.LocalDate;

public record UserCreateResponse(Long id,
                             String name,
                             String email,
                             LocalDate birthDate) {
    public static UserCreateResponse from(UserResult info) {
        return new UserCreateResponse(
            info.id(),
            info.name(),
            info.email(),
            info.birthDate()
        );
    }
}
