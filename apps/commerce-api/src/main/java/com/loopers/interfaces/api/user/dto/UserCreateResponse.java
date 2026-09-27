package com.loopers.interfaces.api.user.dto;

import java.time.LocalDate;

import com.loopers.application.user.dto.UserResult;

public record UserCreateResponse(Long id, String name, String email, LocalDate birthDate) {
    public static UserCreateResponse from(UserResult info) {
        return new UserCreateResponse(info.id(), info.name(), info.email(), info.birthDate());
    }
}
